/*
 * Licensed to Elasticsearch B.V. under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch B.V. licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package co.elastic.apm.agent.sparkjava;

import co.elastic.apm.agent.bci.TracerAwareInstrumentation;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collection;
import java.util.Collections;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class RoutesInstrumentation extends TracerAwareInstrumentation {

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return named("spark.route.Routes");
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return named("find")
            .and(takesArguments(3))
            .and(takesArgument(0, named("spark.route.HttpMethod")))
            .and(takesArgument(1, named("java.lang.String")))
            .and(takesArgument(2, named("java.lang.String")))
            .and(returns(named("spark.routematch.RouteMatch")));
    }

    @Override
    public String getAdviceClassName() {
        return "co.elastic.apm.agent.sparkjava.RoutesAdvice";
    }

    @Override
    public Collection<String> getInstrumentationGroupNames() {
        return Collections.singletonList("sparkjava");
    }
}
