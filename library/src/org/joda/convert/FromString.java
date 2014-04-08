/*
 *  Copyright 2010-present Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.convert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method or constructor as being suitable for converting
 * an object from a {@code String}.
 * <p>
 * When applying to a method, this annotation should be applied once per class.
 * The method must be static and have one {@code String} parameter with a
 * return type of the type that the method is implemented on.
 * For example, {@link Integer#parseInt(String)}.
 * <p>
 * When applying to a constructor, this annotation should be applied to the constructor
 * that takes one {@code String} parameter.
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.RUNTIME)
public @interface FromString {

}
