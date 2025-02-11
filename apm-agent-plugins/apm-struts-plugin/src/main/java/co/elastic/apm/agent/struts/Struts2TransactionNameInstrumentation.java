/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2021 Elastic and contributors
 * %%
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
 * #L%
 */
package co.elastic.apm.agent.struts;

import co.elastic.apm.agent.sdk.ElasticApmInstrumentation;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Collection;

import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Struts2TransactionNameInstrumentation extends ElasticApmInstrumentation {

    @Override
    public ElementMatcher<? super NamedElement> getTypeMatcherPreFilter() {
        return nameContains("ActionProxy");
    }

    @Override
    public final ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return hasSuperType(named("com.opensymphony.xwork2.ActionProxy"));
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return named("execute");
    }

    @Override
    public Collection<String> getInstrumentationGroupNames() {
        return singletonList("struts");
    }

    @Override
    public String getAdviceClassName() {
        return "co.elastic.apm.agent.struts.Struts2TransactionNameAdvice";
    }
}
