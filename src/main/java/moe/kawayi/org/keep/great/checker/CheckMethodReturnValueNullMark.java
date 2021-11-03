//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
// The CheckMethodReturnValueNullMark.java is a part of project utopia, under MIT License.
// See https://opensource.org/licenses/MIT for license information.
// Copyright (c) 2021 moe-org All rights reserved.
//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

package moe.kawayi.org.keep.great.checker;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import moe.kawayi.org.keep.great.checker.util.AstUtil;
import moe.kawayi.org.keep.great.checker.util.NotNull;
import moe.kawayi.org.keep.great.checker.util.Nullable;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 检查方法是否具有nullable或者notnull注解。
 *
 * 可以设置单行设置来跳过检查。单行设置必须放置在函数定义之前，所有注解之后。
 *
 * 如:
 * <pre>
 * {@code
 * // no null mark
 * @A
 * @B
 * public void will_fail(){}
 *
 * @A
 * // no null mark
 * @B
 * public void will_fail_too(){}
 *
 *
 * @A
 * @B
 * // no null check
 * public void success(){}
 *
 * @A
 * @B
 * // other comment
 * // no null check
 * // other comment
 * public void success_too(){}
 * }
 * </pre>
 * 其中will_fail和will_fail_too将不会通过检查。success和success_too将会通过检查。
 *
 *
 * 同时设置两个注解也不会通过检查:
 * <pre>
 * {@code
 * @NotNull
 * @Nullable
 * public void will_fail(){}
 * }
 * </pre>
 *
 * 如果返回值为void，则跳过检查。
 */
public class CheckMethodReturnValueNullMark extends AbstractCheck {

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
                TokenTypes.METHOD_DEF,
        };
    }

    @Override
    @NotNull
    public int[] getAcceptableTokens() {
        return new int[]{
                TokenTypes.METHOD_DEF,
        };
    }

    @Override
    @NotNull
    public int[] getRequiredTokens() {
        return new int[]{
                TokenTypes.METHOD_DEF,
        };
    }

    @Override
    public boolean isCommentNodesRequired() {
        return true;
    }

    private boolean checkIfIgnoreComment(@Nullable DetailAST ast){

        // 检查注释
        while(ast != null){
            // note:只检查单行注释
            if(
                    ast.getType() == TokenTypes.SINGLE_LINE_COMMENT
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

        // 检查是否为void或者其他基本类型（即不可为null的类型）
        if(AstUtil.checkIfNonNullType(ast.findFirstToken(TokenTypes.TYPE))){
            return;
        }

        // input token type should be METHOD_DEF
        // 检查修饰符
        var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);

        if(modifiers == null){
            log(ast,
                    "the method return value does not have a null mark such as {0} or {1}",
                    notnullRegex,
                    nullableRegex);
            return;
        }

        // 检查注释
        var ignoreComment = modifiers.findFirstToken(TokenTypes.SINGLE_LINE_COMMENT);

        if(checkIfIgnoreComment(ignoreComment)){
            // 忽略
            return;
        }

        // 注释无果，检查annotations
        var annotations = modifiers.findFirstToken(TokenTypes.ANNOTATION);

        if(annotations == null){
            log(modifiers,
                    "miss null mark annotation.add a {0} or {1} annotation to mark",
                    notnullRegex,
                    nullableRegex);
            return;
        }

        checkNullMark(annotations);
    }
}
