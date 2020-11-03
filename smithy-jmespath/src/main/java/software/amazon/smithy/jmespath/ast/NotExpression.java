/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.jmespath.ast;

import java.util.Objects;
import software.amazon.smithy.jmespath.ExpressionVisitor;
import software.amazon.smithy.jmespath.JmespathExpression;

/**
 * Negates an expression based on if the wrapped expression is truthy.
 */
public final class NotExpression extends JmespathExpression {

    private final JmespathExpression expression;

    public NotExpression(JmespathExpression expression) {
        this(expression, 1, 1);
    }

    public NotExpression(JmespathExpression expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visitNot(this);
    }

    /**
     * Gets the contained expression to negate.
     *
     * @return Returns the contained expression.
     */
    public JmespathExpression getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof NotExpression)) {
            return false;
        }
        NotExpression notNode = (NotExpression) o;
        return expression.equals(notNode.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return "NotExpression{expression=" + expression + '}';
    }
}
