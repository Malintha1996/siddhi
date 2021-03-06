/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.query.compiler;

import io.siddhi.query.api.SiddhiApp;
import io.siddhi.query.api.definition.AggregationDefinition;
import io.siddhi.query.api.definition.FunctionDefinition;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.definition.TableDefinition;
import io.siddhi.query.api.execution.partition.Partition;
import io.siddhi.query.api.execution.query.OnDemandQuery;
import io.siddhi.query.api.execution.query.Query;
import io.siddhi.query.api.execution.query.StoreQuery;
import io.siddhi.query.api.expression.constant.TimeConstant;
import io.siddhi.query.compiler.exception.SiddhiParserException;
import io.siddhi.query.compiler.internal.SiddhiErrorListener;
import io.siddhi.query.compiler.internal.SiddhiQLBaseVisitorImpl;
import io.siddhi.query.compiler.langserver.LanguageServerParserErrorStrategy;
import io.siddhi.query.compiler.langserver.SiddhiQLLSVisitorImpl;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Siddhi query compiler
 */
public class SiddhiCompiler {

    public static SiddhiApp parse(String source) {
        SiddhiQLParser parser = initParser(source);
        //            parser.setErrorHandler(new BailErrorStrategy());
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.parse();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (SiddhiApp) eval.visit(tree);
    }

    /**
     * Used at the Siddhi Language server to parse source content and obtain a parseTreeMap.
     *
     * @param source
     * @param goalPosition
     * @return
     * @throws RecognitionException
     */
    public static Map<String, ParseTree> parse(String source, int[] goalPosition)
            throws RecognitionException {
        SiddhiQLParser parser = initParser(source);
        parser.setErrorHandler(new LanguageServerParserErrorStrategy());
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        try {
            ParseTree parseTree = parser.parse();
            SiddhiQLLSVisitorImpl visitor = new SiddhiQLLSVisitorImpl(goalPosition);
            parseTree.accept(visitor);
            return visitor.getParseTreeMap();
        } catch (SiddhiParserException ignored) {
            //todo: e has been ignored until it will be written to a log file.
            return ((LanguageServerParserErrorStrategy)
                    parser.getErrorHandler()).getParseTreeMap();

        }
    }

    private static SiddhiQLParser initParser(String source) {
        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        return parser;
    }

    public static StreamDefinition parseStreamDefinition(String source) {

        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.definition_stream_final();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (StreamDefinition) eval.visit(tree);
    }

    public static TableDefinition parseTableDefinition(String source) throws SiddhiParserException {

        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.definition_table_final();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (TableDefinition) eval.visit(tree);
    }

    public static AggregationDefinition parseAggregationDefinition(String source) throws SiddhiParserException {

        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.definition_aggregation_final();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (AggregationDefinition) eval.visit(tree);
    }

    public static Partition parsePartition(String source) throws SiddhiParserException {

        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.partition_final();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (Partition) eval.visit(tree);
    }

    public static Query parseQuery(String source) throws SiddhiParserException {

        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.query_final();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (Query) eval.visit(tree);
    }

    public static FunctionDefinition parseFunctionDefinition(String source) throws SiddhiParserException {
        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.definition_function_final();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (FunctionDefinition) eval.visit(tree);
    }

    public static TimeConstant parseTimeConstantDefinition(String source) throws SiddhiParserException {
        ANTLRInputStream input = new ANTLRInputStream(source);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.time_value();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (TimeConstant) eval.visit(tree);
    }

    public static OnDemandQuery parseOnDemandQuery(String onDemandQuery) throws SiddhiParserException {

        ANTLRInputStream input = new ANTLRInputStream(onDemandQuery);
        SiddhiQLLexer lexer = new SiddhiQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SiddhiErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        SiddhiQLParser parser = new SiddhiQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(SiddhiErrorListener.INSTANCE);
        ParseTree tree = parser.store_query_final();

        SiddhiQLVisitor eval = new SiddhiQLBaseVisitorImpl();
        return (OnDemandQuery) eval.visit(tree);
    }

    public static StoreQuery parseStoreQuery(String storeQuery) throws SiddhiParserException {
        OnDemandQuery onDemandQuery = parseOnDemandQuery(storeQuery);
        return new StoreQuery(onDemandQuery);
    }

    public static String updateVariables(String siddhiApp) {
        String updatedSiddhiApp = siddhiApp;
        if (siddhiApp.contains("$")) {
            Pattern variablePattern = Pattern.compile("\\$\\{(\\w+)\\}");
            Matcher variableMatcher = variablePattern.matcher(siddhiApp);
            while (variableMatcher.find()) {
                String key = variableMatcher.group(1);
                String value = System.getProperty(key);
                if (value == null) {
                    value = System.getenv(key);
                    if (value == null) {
                        int startIndex = variableMatcher.start(0);
                        int endIndex = variableMatcher.end(0);
                        String upToStartIndex = siddhiApp.substring(0, startIndex);
                        int lineStartIndex = upToStartIndex.lastIndexOf("\n") + 1;
                        int lineNumber = upToStartIndex.split("\n").length;
                        Pattern appNamePattern = Pattern.compile("@app:name\\(\\W*('|\")(\\w+)('|\")\\W*\\)");
                        Matcher appNameMatcher = appNamePattern.matcher(siddhiApp);
                        if (appNameMatcher.find()) {
                            String appName = appNameMatcher.group(2);
                            throw new SiddhiParserException("No system or environmental variable found for '${"
                                    + key + "}', for Siddhi App '" + appName + "'",
                                    new int[]{lineNumber, startIndex - lineStartIndex},
                                    new int[]{lineNumber, endIndex - lineStartIndex});
                        } else {
                            throw new SiddhiParserException("No system or environmental variable found for '${"
                                    + key + "}'", new int[]{lineNumber, startIndex - lineStartIndex},
                                    new int[]{lineNumber, endIndex - lineStartIndex});
                        }
                    }
                }
                updatedSiddhiApp = updatedSiddhiApp.replaceAll("\\$\\{(" + key + ")\\}", value);
            }
        }
        return updatedSiddhiApp;
    }
}
