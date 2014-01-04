/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.sql.naming;

/**
 * Name of a database object such as a table or schema.
 *
 * <p>Its {@link #equals(Object)}, {@link #hashCode()} and
 * {@link #compareTo(Object)} methods implement the naming policy of the
 * {@link org.eigenbase.sql.naming.NamingFactory} that created it.</p>
 */
public interface Name extends Comparable<Name> {
}

// End Name.java
