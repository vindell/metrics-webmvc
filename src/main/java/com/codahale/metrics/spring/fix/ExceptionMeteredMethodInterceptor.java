/*
 * Copyright (c) 2018 (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.codahale.metrics.spring.fix;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.webmvc.aspect.AnnotationFilter;
import com.codahale.metrics.webmvc.utils.MetricUtils;

public class ExceptionMeteredMethodInterceptor extends AbstractMetricMethodInterceptor<ExceptionMetered, Meter> implements Ordered {

	public static final Class<ExceptionMetered> ANNOTATION = ExceptionMetered.class;
	public static final Pointcut POINTCUT = new AnnotationMatchingPointcut(null, ANNOTATION);
	public static final MethodFilter METHOD_FILTER = new AnnotationFilter(ANNOTATION, AnnotationFilter.PROXYABLE_METHODS);

	public ExceptionMeteredMethodInterceptor(final MetricRegistry metricRegistry) {
		super(metricRegistry, ANNOTATION, METHOD_FILTER);
	}

	@Override
	protected Object invoke(MethodInvocation invocation, Meter meter, ExceptionMetered annotation) throws Throwable {
		try {
			return invocation.proceed();
		}
		catch (Throwable t) {
			if (annotation.cause().isAssignableFrom(t.getClass())) {
				meter.mark();
			}
			throw t;
		}
	}

	@Override
	protected Meter buildMetric(MetricRegistry metricRegistry, String metricName, ExceptionMetered annotation) {
		return metricRegistry.meter(metricName);
	}

	@Override
	protected String buildMetricName(Class<?> targetClass, Method method, ExceptionMetered annotation) {
		return MetricUtils.forExceptionMeteredMethod(targetClass, method, annotation);
	}
	
	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
