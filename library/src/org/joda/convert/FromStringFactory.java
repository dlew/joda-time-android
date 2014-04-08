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
 * Annotation used on a type to indicate that another class, the factory,
 * provides the 'from string' method.
 * <p>
 * This annotation is applied at the type level, typically to an interface.
 * It indicates the class which contains the relevant {@code FromString}
 * annotation, which follows the normal rules.
 * <p>
 * For example, the interface {@code Foo} could be annotated to define its
 * associated factory as being {@code FooFactory}. The {@code FooFactory}
 * class would then be expected to provide a method returning {@code Foo}
 * with a single {@code String} parameter, annotated with {@code FromString}.
 * 
 * @since 1.4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FromStringFactory {

    /**
     * The factory class containing the static method.
     * The static method must have a return type of the type that declares
     * the factory annotation.
     */
    Class<?> factory();

}
