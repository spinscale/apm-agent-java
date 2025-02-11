/*-
 * #%L
 * Elastic APM Java agent
 * %%
 * Copyright (C) 2018 - 2020 Elastic and contributors
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
package co.elastic.apm.agent.hibernatesearch.v5;

import co.elastic.apm.agent.bci.TracerAwareInstrumentation;
import co.elastic.apm.agent.hibernatesearch.HibernateSearchConstants;
import co.elastic.apm.agent.hibernatesearch.HibernateSearchHelper;
import co.elastic.apm.agent.impl.transaction.Span;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.hibernate.search.query.hibernate.impl.FullTextQueryImpl;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

import static co.elastic.apm.agent.bci.bytebuddy.CustomElementMatchers.classLoaderCanLoadClass;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isBootstrapClassLoader;
import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class HibernateSearch5Instrumentation extends TracerAwareInstrumentation {

    @Override
    public String getAdviceClassName() {
        return "co.elastic.apm.agent.hibernatesearch.v5.HibernateSearch5Instrumentation$HibernateSearch5ExecuteAdvice";
    }

    @Override
    public ElementMatcher<? super NamedElement> getTypeMatcherPreFilter() {
        return nameContains("Query");
    }

    @Override
    public ElementMatcher.Junction<ClassLoader> getClassLoaderMatcher() {
        return not(isBootstrapClassLoader())
            .and(classLoaderCanLoadClass("org.hibernate.search.FullTextQuery"));
    }

    @Override
    public ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return not(isInterface())
            .and(hasSuperType(named("org.hibernate.search.FullTextQuery")));
    }

    @Override
    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return named("list")
            .or(named("scroll"))
            .or(named("iterate"));
    }

    @Override
    public Collection<String> getInstrumentationGroupNames() {
        return Collections.singleton(HibernateSearchConstants.HIBERNATE_SEARCH_ORM_TYPE);
    }

    public static class HibernateSearch5ExecuteAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class, inline = false)
        public static Object onBeforeExecute(@Advice.This FullTextQueryImpl query,
                                              @Advice.Origin("#m") String methodName) {

            return HibernateSearchHelper.createAndActivateSpan(tracer, methodName, query.getQueryString());
        }

        @Advice.OnMethodExit(suppress = Throwable.class, onThrowable = Throwable.class, inline = false)
        public static void onAfterExecute(@Advice.Enter @Nullable Object span,
                                          @Advice.Thrown Throwable t) {

            if (span instanceof Span) {
                ((Span) span).captureException(t)
                    .deactivate()
                    .end();
            }
        }
    }
}
