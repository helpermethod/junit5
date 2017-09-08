/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Status.STABLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;

/**
 * {@code @ExtendWith} is a {@linkplain Repeatable repeatable} annotation
 * that is used to register {@linkplain Extension extensions} for the
 * annotated test class or test method.
 *
 * <h3>Supported Extension APIs</h3>
 * <ul>
 * <li>{@link ExecutionCondition}</li>
 * <li>{@link BeforeAllCallback}</li>
 * <li>{@link AfterAllCallback}</li>
 * <li>{@link BeforeEachCallback}</li>
 * <li>{@link AfterEachCallback}</li>
 * <li>{@link BeforeTestExecutionCallback}</li>
 * <li>{@link AfterTestExecutionCallback}</li>
 * <li>{@link TestInstancePostProcessor}</li>
 * <li>{@link ParameterResolver}</li>
 * <li>{@link TestExecutionExceptionHandler}</li>
 * </ul>
 *
 * @since 5.0
 * @see Extension
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(Extensions.class)
@API(status = STABLE, since = "5.0")
public @interface ExtendWith {

	/**
	 * An array of one or more {@link Extension} classes to register.
	 */
	Class<? extends Extension>[] value();

}
