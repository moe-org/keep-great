//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
// The CheckMethodArugmentsNullMark.java is a part of project utopia, under MIT License.
// See https://opensource.org/licenses/MIT for license information.
// Copyright (c) 2021 moe-org All rights reserved.
//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

package moe.kawayi.org.keep.great.checker;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import moe.kawayi.org.keep.great.checker.util.AstUtil;
import moe.kawayi.org.keep.great.checker.util.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 检查函数参数null mark
 */
public class CheckMethodArgumentsNullMark extends AbstractCheck {

    private String nullableRegex = "^Nullable$";

    private String notnullRegex = "^NotNull$";

    private String ignoreCheckMarkRegex = "^\\s*no-null-mark\\s*$";

    public void setNullableRegex(@NotNull String regex){
        nullableRegex = regex;
    }
    public void setNotnullRegex(@NotNull String regex){
        notnullRegex = regex;
    }
    public void setIgnoreCheckMarkRegex(@NotNull String regex) {ignoreCheckMarkRegex = regex;}

    @Override
    @NotNull
    public int[] getDefaultTokens() {
        return new int[]{
                TokenTypes.PARAMETER_DEF,
        };
    }

    @Override
    @NotNull
    public int[] getAcceptableTokens() {
        return new int[]{
                TokenTypes.PARAMETER_DEF,
        };
    }

    @Override
    @NotNull
    public int[] getRequiredTokens() {
        return new int[]{
                TokenTypes.PARAMETER_DEF,
        };
    }


    @Override
    public boolean isCommentNodesRequired() {
        return true;
    }


    private boolean checkIgnoreComment(@NotNull DetailAST ast){
        Objects.requireNonNull(ast);

        // 检查注释
        while(ast != null){
            // note:只检查多行注释
            if(
                    ast.getType() == TokenTypes.BLOCK_COMMENT_BEGIN
                    &&
                    Pattern.matches(
                        ignoreCheckMarkRegex,
                        ast.findFirstToken(TokenTypes.COMMENT_CONTENT).getText())){
                return true;
            }

            ast = ast.getNextSibling();
        }

        return false;
    }

    private void checkNullMark(@NotNull DetailAST ast){
        Objects.requireNonNull(ast);

        boolean nullAble = false;
        boolean notNull = false;

        var lastNotNullAst = ast;

        // 循环查找null mark annotations
        while(ast != null){
            lastNotNullAst = ast;

            if(ast.getType() == TokenTypes.ANNOTATION) {
                if (Pattern.matches(
                        nullableRegex,
                        ast.findFirstToken(TokenTypes.IDENT).getText())) {
                    nullAble = true;
                }
                if (Pattern.matches(
                        notnullRegex,
                        ast.findFirstToken(TokenTypes.IDENT).getText())) {
                    notNull = true;
                }
            }

            ast = ast.getNextSibling();
        }

        // 检查
        if(notNull&&nullAble)
            log(lastNotNullAst,"set null able and not null annotations at same time!");

        if(!(notNull||nullAble))
            log(lastNotNullAst,"not found null mark annotation!");
    }


    @Override
    public void visitToken(@NotNull DetailAST ast) {
        // 检查父对象
        var parent = ast.getParent();
        if(parent == null || parent.getType() != TokenTypes.PARAMETERS){
            return;
        }

        var pParent = parent.getParent();
        if(pParent == null || pParent.getType() != TokenTypes.METHOD_DEF){
            return;
        }

        var type = ast.findFirstToken(TokenTypes.TYPE);

        // 检查类型
        if(AstUtil.checkIfNonNullType(type)){
            return;
        }

        // 检查注释。注释于type
        if(checkIgnoreComment(type.getFirstChild())){
            return;
        }

        // 检查注解
        var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);

        if(modifiers == null){
            log(ast,"the method parameter does not have a null mark such as {0} or {1}",nullableRegex,notnullRegex);
            return;
        }

        var annotation = modifiers.findFirstToken(TokenTypes.ANNOTATION);

        if(annotation == null){
            log(ast,"the method parameter does not have a null mark such as {0} or {1}",nullableRegex,notnullRegex);
            return;
        }

        checkNullMark(annotation);
    }


}
