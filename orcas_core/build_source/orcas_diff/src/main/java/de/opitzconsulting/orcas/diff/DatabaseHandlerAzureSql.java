package de.opitzconsulting.orcas.diff;

import de.opitzconsulting.orcas.orig.diff.ColumnDiff;
import de.opitzconsulting.orcas.sql.CallableStatementProvider;
import de.opitzconsulting.orcas.sql.WrapperExecutePreparedStatement;
import de.opitzconsulting.origOrcasDsl.CharType;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.opitzconsulting.origOrcasDsl.OrigOrcasDslPackage.Literals.COLUMN__DEFAULT_VALUE;

public class DatabaseHandlerAzureSql extends DatabaseHandler {
    @Override
    public void createOrcasUpdatesTable(String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider) {
        String lSql = "create table " + pOrcasUpdatesTableName + " ( scup_id int not null IDENTITY, scup_script_name varchar(4000) not null, scup_logname varchar(100) not null, scup_date date not null, scup_schema varchar(30) not null, primary key (scup_id))";
        new WrapperExecutePreparedStatement(lSql, pOrcasCallableStatementProvider).execute();
    }

    @Override
    public String getSequenceDefaultMaxValueString() {
        return "9223372036854775807";
    }

    @Override
    public String getSequenceDefaultMinValueString() {
        return "-9223372036854775808";
    }

    @Override
    public void insertIntoOrcasUpdatesTable(String pOrcasUpdatesTableName, CallableStatementProvider pOrcasCallableStatementProvider, String pFilePart, String pLogname) {
        String lSql = "" + //
                " insert into " + pOrcasUpdatesTableName + "(" + //
                "        scup_script_name," + //
                "        scup_date," + //
                "        scup_schema," + //
                "        scup_logname" + //
                "        )" + //
                " values (" + //
                "        ?," + //
                "        getutcdate()," + //
                "        schema_name()," + //
                "        ?" + //
                "        )" + //
                "";
        List<Object> lInsertParameters = new ArrayList<Object>();
        lInsertParameters.add(pFilePart);
        lInsertParameters.add(pLogname);
        new WrapperExecutePreparedStatement(lSql, pOrcasCallableStatementProvider, lInsertParameters).execute();
    }

    @Override
    public LoadIst createLoadIst(CallableStatementProvider pCallableStatementProvider, Parameters pParameters) {
        return new LoadIstAzureSql(pCallableStatementProvider, pParameters);
    }

    @Override
    public CharType getDefaultCharType(CallableStatementProvider pCallableStatementProvider) {
        return CharType.CHAR;
    }

    @Override
    public String getDefaultTablespace(CallableStatementProvider pCallableStatementProvider) {
        return null;
    }

    @Override
    public DdlBuilder createDdlBuilder(Parameters pParameters) {
        return new DdlBuilderAzureSql(pParameters, this);
    }

    @Override
    public void executeDiffResultStatement(String pStatementToExecute, CallableStatementProvider pCallableStatementProvider) {
        new WrapperExecutePreparedStatement(pStatementToExecute, pCallableStatementProvider).execute();
    }

    @Override
    public boolean isRenamePrimaryKey() {
        return false;
    }

    @Override
    public boolean isRenameIndex() {
        return false;
    }

    @Override
    public boolean isRenameMView() {
        return false;
    }

    @Override
    public boolean isRenameForeignKey() {
        return false;
    }

    @Override
    public boolean isRenameUniqueKey() {
        return false;
    }

    @Override
    public boolean isRenameConstraint() {
        return false;
    }

    @Override
    public boolean isUpdateIdentity() {
        return false;
    }

    @Override
    protected boolean isExpressionDifferentNotNull(String pExpression1, String pExpression2) {
        return isExpressionDifferentNotNullStatic(pExpression1, pExpression2);
    }

    public static boolean isExpressionDifferentNotNullStatic(String pExpression1, String pExpression2) {
        return !cleanupExpression(pExpression1).equals(cleanupExpression(pExpression2));
    }

    @Override
    public int getDefaultFloatPrecision() {
        return 53;
    }

    @Override
    public Integer getDefaultNumberPrecision() {
        return 18;
    }

    private static String cleanupSubExpression(String pExpression) {
        String lReturn = pExpression;

        lReturn = lReturn.trim();

        lReturn = lReturn.toLowerCase();

        lReturn = lReturn.replace("current_timestamp", "now");

        lReturn = lReturn.replace("[", "");
        lReturn = lReturn.replace("]", "");
        lReturn = lReturn.replace(" ", "");

        lReturn = lReturn.replace("!=", "<>");

        return lReturn;
    }

    private abstract static class Token {
        @Override
        public abstract String toString();
    }

    private static class ConstantToken extends Token {
        String token;

        public ConstantToken(String token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return token;
        }
    }

    private static class UnparsedToken extends Token {
        String token;

        public UnparsedToken(String token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return cleanupSubExpression(token);
        }

        public Token convertBetween() {
            Pattern pattern = Pattern.compile("(.+) between (.+) and (.+)");
            Matcher matcher = pattern.matcher(token.toLowerCase());

            if (matcher.matches()) {
                return new UnparsedToken(matcher.group(1) + ">=" + matcher.group(2) + " and " + matcher.group(1) + "<=" + matcher.group(3));
            } else {
                return this;
            }
        }

        public List<Token> splitByIfPossible(String pSplit) {
            if (!token.toLowerCase().contains(pSplit)) {
                return null;
            }

            String lRemaining = token.toLowerCase();
            List<Token> lReturn = new ArrayList<>();

            do {
                lReturn.add(new UnparsedToken(lRemaining.substring(0, lRemaining.indexOf(pSplit))));
                lRemaining = lRemaining.substring(lRemaining.indexOf(pSplit) + pSplit.length());
            } while (lRemaining.contains(pSplit));

            lReturn.add(new UnparsedToken(lRemaining));

            return lReturn;
        }
    }

    @Override
    public boolean isCanDiffFunctionBasedIndexExpression() {
        return false;
    }

    @Override
    public boolean isRecreateDependenciesForColumn(ColumnDiff pColumnDiff) {
        return (!pColumnDiff.notnullIsEqual
                || !pColumnDiff.byteorcharIsEqual
                || !pColumnDiff.scaleIsEqual
                || !pColumnDiff.precisionIsEqual
        ) && pColumnDiff.isOld && pColumnDiff.isNew;
    }

    @Override
    public List<RecreateNeededBuilder.Difference> isRecreateColumn(ColumnDiff pColumnDiff) {
        List<RecreateNeededBuilder.Difference> lReturn = super.isRecreateColumn(pColumnDiff);

        if ("virtual".equals(pColumnDiff.virtualNew) && !pColumnDiff.default_valueIsEqual) {
            if (isExpressionDifferent(pColumnDiff.default_valueNew, pColumnDiff.default_valueOld)) {
                lReturn.add(new RecreateNeededBuilder.DifferenceImpl(COLUMN__DEFAULT_VALUE, pColumnDiff));
            }
        }

        return lReturn;
    }

    private static class SubListToken extends Token {
        List<Token> tokens;
        boolean logicOr = false;
        boolean logicAnd = false;

        public SubListToken(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public String toString() {
            String lLogicCombine = "";

            if (logicAnd) {
                lLogicCombine = "and";
            }
            if (logicOr) {
                lLogicCombine = "or";
            }

            return (lLogicCombine.isEmpty() ? "" : "(")
                    + tokens.stream().map(Object::toString).collect(Collectors.joining(lLogicCombine))
                    + (lLogicCombine.isEmpty() ? "" : ")");
        }

        public void convertBetweenRecursive() {
            tokens = tokens.stream().map(it -> {
                if (it instanceof UnparsedToken) {
                    return ((UnparsedToken) it).convertBetween();
                } else {
                    if (it instanceof SubListToken) {
                        ((SubListToken) it).convertBetweenRecursive();
                    }
                    return it;
                }
            }).collect(Collectors.toList());
        }

        public void convertInRecursive() {
            boolean lConverted = false;

            List<Token> filteredTokenList = tokens.stream().filter(it -> (!(it instanceof UnparsedToken)) || !((UnparsedToken) it).token.isEmpty()).collect(Collectors.toList());
            if (filteredTokenList.size() == 2) {
                if (filteredTokenList.get(0) instanceof UnparsedToken) {
                    Pattern pattern = Pattern.compile("(.+) in");
                    Matcher matcher = pattern.matcher(((UnparsedToken) filteredTokenList.get(0)).token.toLowerCase().trim());

                    if (matcher.matches()) {
                        if (filteredTokenList.get(1) instanceof SubListToken) {
                            List<Token> lSubTokenList = ((SubListToken) filteredTokenList.get(1)).tokens;
                            if (lSubTokenList.size() > 0) {
                                lConverted = true;

                                tokens.clear();
                                logicOr = true;

                                if (lSubTokenList.size() == 1 && lSubTokenList.get(0) instanceof UnparsedToken) {
                                    List<String> strings = Arrays.stream(((UnparsedToken) lSubTokenList.get(0)).token.split(",")).collect(Collectors.toList());

                                    strings.forEach(it -> tokens.add(new UnparsedToken(matcher.group(1) + " = " + it)));
                                } else {
                                    lSubTokenList.stream().filter(it -> it instanceof ConstantToken).forEach(it -> tokens.add(new SubListToken(Stream.of(new UnparsedToken(matcher.group(1) + " = "), it).collect(Collectors.toList()))));
                                }
                            }
                        }
                    }
                }
            }

            if (!lConverted) {
                for (Token lToken : tokens) {
                    if (lToken instanceof SubListToken) {
                        ((SubListToken) lToken).convertInRecursive();
                    }
                }
            }
        }

        public void convertLogicRecursive() {
            handleLogic(false);
            if (!logicOr) {
                handleLogic(true);
            }

            for (Token lToken : tokens) {
                if (lToken instanceof SubListToken) {
                    ((SubListToken) lToken).convertLogicRecursive();
                }
            }

            tokens = tokens
                    .stream()
                    .filter(it -> !it.toString().isEmpty())
                    .collect(Collectors.toList());

            if (logicOr || logicAnd) {
                tokens = tokens
                        .stream()
                        .flatMap(it -> {
                            if (it instanceof SubListToken) {
                                SubListToken lSubListToken = (SubListToken) it;
                                if ((lSubListToken.tokens.size() < 2) ||
                                        lSubListToken.logicOr == logicOr && lSubListToken.logicAnd == logicAnd
                                ) {
                                    return lSubListToken.tokens.stream();
                                }
                            }

                            return Stream.of(it);
                        })
                        .sorted(Comparator.comparing(Objects::toString))
                        .collect(Collectors.toList());
            }
        }

        public void handleLogic(boolean pAnd) {
            List<List<Token>> lNewTokens = new ArrayList<>();
            List<Token> lNewSubTokens = new ArrayList<>();
            boolean lLogicFound = false;

            BiConsumer<Boolean, Token> newTokenHandler = (isNewGroup, newToken) -> {
                if (isNewGroup) {
                    lNewTokens.add(new ArrayList<>(lNewSubTokens));
                    lNewSubTokens.clear();
                }

                lNewSubTokens.add(newToken);
            };

            for (Token lToken : tokens) {
                if (lToken instanceof UnparsedToken) {
                    List<Token> lOrSplitTokens = ((UnparsedToken) lToken).splitByIfPossible(pAnd ? " and " : " or ");

                    if (lOrSplitTokens != null) {
                        lLogicFound = true;


                        for (int i = 0; i < lOrSplitTokens.size(); i++) {
                            newTokenHandler.accept(i > 0, lOrSplitTokens.get(i));
                        }
                    } else {
                        newTokenHandler.accept(false, lToken);
                    }
                } else {
                    newTokenHandler.accept(false, lToken);
                }
            }

            lNewTokens.add(new ArrayList<>(lNewSubTokens));

            if (lLogicFound) {
                if (pAnd) {
                    logicAnd = true;
                } else {
                    logicOr = true;
                }

                tokens.clear();
                lNewTokens.forEach(it -> {
                    if (it.size() > 0) {
                        tokens.add(new SubListToken(it));
                    }
                });
            }
        }
    }

    public static String cleanupExpression(String pExpression) {
        List<Token> lList = new ArrayList<>();

        String lNormalString = "";
        String lConstantString = "";

        boolean lIsIn = false;
        for (int i = 0; i < pExpression.length(); i++) {
            char c = pExpression.charAt(i);

            if (c == '\'') {
                if (lIsIn && (pExpression.length() > (i + 1)) && pExpression.charAt(i + 1) == '\'') {
                    lConstantString += "'";
                    i++;
                } else {
                    lIsIn = !lIsIn;

                    if (lIsIn) {
                        if (!lNormalString.isEmpty()) {
                            lList.add(new UnparsedToken(lNormalString));
                        }
                        lNormalString = "";

                        lConstantString = "'";
                    } else {
                        lConstantString += "'";

                        lList.add(new ConstantToken(lConstantString));
                    }
                }
            } else {
                if (lIsIn) {
                    lConstantString += c;
                } else {
                    lNormalString += c;
                }
            }
        }
        if (!lNormalString.isEmpty()) {
            lList.add(new UnparsedToken(lNormalString));
        }

        SubListToken subListToken = new SubListToken(handleBraces(lList));

        subListToken.convertBetweenRecursive();
        subListToken.convertLogicRecursive();
        subListToken.convertInRecursive();

        subListToken.convertLogicRecursive();

        return subListToken.toString();
    }

    private static List<Token> handleBraces(List<Token> pList) {
        List<Token> lReturn = new ArrayList<>();
        List<Token> lSubList = new ArrayList<>();

        String lNormalString = "";
        String lBraceString = "";


        int lBraceDepth = 0;

        for (Token lToken : pList) {
            if (lToken instanceof UnparsedToken) {
                UnparsedToken lUnparsedToken = (UnparsedToken) lToken;

                for (int j = 0; j < lUnparsedToken.token.length(); j++) {
                    char c = lUnparsedToken.token.charAt(j);

                    if (c == '(') {
                        if (lBraceDepth == 0) {
                            if (!lNormalString.isEmpty()) {
                                lReturn.add(new UnparsedToken(lNormalString));
                            }
                            lNormalString = "";
                        } else {
                            lBraceString += c;
                        }

                        lBraceDepth++;
                    } else {
                        if (c == ')') {
                            lBraceDepth--;

                            if (lBraceDepth == 0) {
                                lSubList.add(new UnparsedToken(lBraceString));
                                lBraceString = "";
                                lReturn.add(new SubListToken(handleBraces(lSubList)));
                                lSubList = new ArrayList<>();
                            } else {
                                lBraceString += c;
                            }
                        } else {
                            if (lBraceDepth == 0) {
                                lNormalString += c;
                            } else {
                                lBraceString += c;
                            }
                        }
                    }
                }

                if (lBraceDepth == 0) {
                    if (!lNormalString.isEmpty()) {
                        lReturn.add(new UnparsedToken(lNormalString));
                    }
                    lNormalString = "";
                } else {
                    lSubList.add(new UnparsedToken(lBraceString));
                    lBraceString = "";
                }
            } else {
                if (lBraceDepth == 0) {
                    lReturn.add(lToken);
                } else {
                    lSubList.add(lToken);
                }
            }
        }

        if (lBraceDepth != 0) {
            return pList;
        }

        return lReturn;
    }
}
