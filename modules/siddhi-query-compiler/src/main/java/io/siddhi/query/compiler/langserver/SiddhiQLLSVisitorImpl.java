/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.query.compiler.langserver;

import io.siddhi.query.compiler.SiddhiQLBaseVisitor;
import io.siddhi.query.compiler.SiddhiQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SiddhiQLLSVisitorImpl class is the implementation which enables visiting the ParseTree generated by Siddhi parser
 * and build a parseTreeMap to be used by the completion providers of Siddhi Language Server.
 */
public class SiddhiQLLSVisitorImpl extends SiddhiQLBaseVisitor {
    public static final int START_LINE_KEY = 0;
    public static final int START_COLUMN_KEY = 1;
    public static final int END_LINE_KEY = 2;
    public static final int END_COLUMN_KEY = 3;
    private int[] currentPosition;
    private int[] goalPosition;
    private boolean positionIsFound = false;
    private Map<String, ParseTree> parseTreeMap;

    public SiddhiQLLSVisitorImpl(int[] goalPosition) {
        this.parseTreeMap = new LinkedHashMap<>();
        this.currentPosition  = new int[4];
        this.goalPosition = new int[]{goalPosition[0], goalPosition[1]};
    }

    @Override
    public Object visitParse(SiddhiQLParser.ParseContext context) {
        currentPosition[START_LINE_KEY] = 0;
        currentPosition[START_COLUMN_KEY] = 0;
        currentPosition[END_LINE_KEY] = 0;
        currentPosition[END_COLUMN_KEY] = 0;
        parseTreeMap.put(context.getClass().getName(), context);
        visit(context.siddhi_app());
        return this.parseTreeMap;
    }

    @Override
    public Object visit(ParseTree parseTree) {
        if (buildContextPath((ParserRuleContext) parseTree)) {
            parseTree.accept(this);
            if (this.positionIsFound) {
                return this.parseTreeMap;
            }
        }
        return false;
    }

    @Override
    public Boolean visitChildren(RuleNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            if (!node.getChild(i).getClass().equals(TerminalNodeImpl.class)) {
                ParserRuleContext child = (ParserRuleContext) node.getChild(i);
                if (buildContextPath(child)) {
                    child.accept(this);
                }
                if (this.positionIsFound) {
                    break;
                }
            }
        }
        return this.positionIsFound;
    }

    @Override
    public Boolean visitTerminal(TerminalNode terminalNode) {
        currentPosition[START_LINE_KEY] = terminalNode.getSymbol().getLine();
        currentPosition[END_LINE_KEY] = terminalNode.getSymbol().getLine();
        currentPosition[START_COLUMN_KEY] = terminalNode.getSymbol().getCharPositionInLine();
        currentPosition[END_COLUMN_KEY] = terminalNode.getSymbol().getCharPositionInLine() +
                terminalNode.getSymbol().getStopIndex() - terminalNode.getSymbol().getStartIndex();
        if (isWithinContext()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *Finds whether the goal position is in the context, if so the context is added to the parse tree map and returns a
     * boolean value indicating whether the context is in the right path to the goal or not.
     * @param context context that is checked for the goal position.
     * @return whether the direction is correct or not.
     */
    private boolean buildContextPath(ParserRuleContext context) {
        boolean direction = false;
        currentPosition[START_LINE_KEY] = context.getStart().getLine();
        currentPosition[END_LINE_KEY] = context.getStop().getLine();
        currentPosition[START_COLUMN_KEY] = context.getStart().getCharPositionInLine();
        currentPosition[END_COLUMN_KEY] = context.getStart().getCharPositionInLine() + context.getStop().getStopIndex()
                - context.getStart().getStartIndex();
        if (isWithinContext()) {
            direction = true;
            this.parseTreeMap.put(context.getClass().getName(), context);
            for (int i = 0; i < context.getChildCount(); i++) {
                if (context.getChild(i).getClass().equals(TerminalNodeImpl.class)) {
                    if (visitTerminal((TerminalNodeImpl) context.getChild(i))) {
                        this.parseTreeMap.put(context.getChild(i).getClass().getName(), context.getChild(i));
                        this.positionIsFound = true;
                        break;
                    }
                }
            }
        }
        return direction;
    }

    private boolean isWithinContext() {
       return (currentPosition[START_LINE_KEY] <= goalPosition[0]
                && goalPosition[0] <= currentPosition[END_LINE_KEY]
                && currentPosition[START_COLUMN_KEY] <= goalPosition[1]
                && goalPosition[1] <= currentPosition[END_COLUMN_KEY]);
    }

    public Map<String, ParseTree> getParseTreeMap() {
        return this.parseTreeMap;
    }
}
