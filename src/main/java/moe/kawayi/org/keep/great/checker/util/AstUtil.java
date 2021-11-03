//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
// The AstUtil.java is a part of project utopia, under MIT License.
// See https://opensource.org/licenses/MIT for license information.
// Copyright (c) 2021 moe-org All rights reserved.
//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

package moe.kawayi.org.keep.great.checker.util;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * ast工具类
 */
public class AstUtil {

    /**
     * 如果输入的类型是不为null的类型（void和int等基本类型）， 返回true
     * @param ast
     * @return
     */
    public static boolean checkIfNonNullType(
            /* others */
            /* no-null-mark */
            /* others */
            @NotNull DetailAST ast){
        return
                // 不为null
                ast != null
                        &&
                        // 只有一个子ast（即基本类型关键字本身）
                        // 防止int[] 等被当作基本类型
                        ast.getChildCount() == 1
                        &&
                        // 查找基本类型关键字
                        (
                                ast.findFirstToken(TokenTypes.LITERAL_VOID) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_BYTE) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_SHORT) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_INT) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_LONG) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_FLOAT) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_DOUBLE) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_BOOLEAN) != null
                                        || ast.findFirstToken(TokenTypes.LITERAL_CHAR) != null
                        );
    }











}
