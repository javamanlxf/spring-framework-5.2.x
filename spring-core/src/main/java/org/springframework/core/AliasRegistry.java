/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core;

/**
 * 用于管理别名的通用接口,
 * 作为{@link org.springframework.beans.factory.support.BeanDefinitionRegistry}父接口
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 */
public interface AliasRegistry {

	/**
	 * 给定名称，为其注册一个别名。
	 * @param name 规范名称
	 * @param alias 要注册的别名
	 * @throws IllegalStateException 别名已经被使用了，并且不能覆盖
	 */
	void registerAlias(String name, String alias);

	/**
	 * 从注册器中移除注册的别名
	 * @param alias 要移除的别名
	 * @throws IllegalStateException 未发现别名
	 */
	void removeAlias(String alias);

	/**
	 * 判断给出的名称是否是一个别名
	 * @param name 要检查的名称
	 * @return 是否是别名
	 */
	boolean isAlias(String name);

	/**
	 * 返回给定名称的别名数组
	 * @param name 名称
	 * @return 别名数组，如果没有为空数组
	 */
	String[] getAliases(String name);

}
