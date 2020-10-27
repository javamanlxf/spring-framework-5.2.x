/*
 * Copyright 2002-2018 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

/**
 * 包含bean定义的注册表的接口，例如RootBeanDefinition和ChildBeanDefinition实例。
 * 通常由内部与AbstractBeanDefinition层次结构一起工作的BeanFactories实现。
 *
 * <p>这是Spring的bean工厂包中唯一封装了bean定义注册的接口。
 * 标准BeanFactory接口仅涵盖对完全配置的工厂实例的访问。
 *
 * <p>Spring的Bean定义读取器希望使用此接口的实现。
 * Spring核心中的已知读取器是DefaultListableBeanFactory和GenericApplicationContext。
 *
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see org.springframework.beans.factory.config.BeanDefinition
 * @see AbstractBeanDefinition
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 * @see DefaultListableBeanFactory
 * @see org.springframework.context.support.GenericApplicationContext
 * @see org.springframework.beans.factory.xml.XmlBeanDefinitionReader
 * @see PropertiesBeanDefinitionReader
 */
public interface BeanDefinitionRegistry extends AliasRegistry {

	/**
	 * 在此注册表中注册新的bean定义。 必须支持RootBeanDefinition和ChildBeanDefinition。
	 * @param beanName 要注册的bean实例的名称
	 * @param beanDefinition 要注册的bean实例的bean定义
	 * @throws BeanDefinitionStoreException bean定义无效
	 * @throws BeanDefinitionOverrideException 如果指定的bean名称已经有一个BeanDefinition，并且不允许我们覆盖它
	 * @see GenericBeanDefinition
	 * @see RootBeanDefinition
	 * @see ChildBeanDefinition
	 */
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException;

	/**
	 * 移除给定名称对应的bean定义
	 * @param beanName 要注册的bean实例的名称
	 * @throws NoSuchBeanDefinitionException 没有对应的bean定义
	 */
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 返回指定名称对应的bean定义
	 * @param beanName 要查找的bean定义对应的名称
	 * @return 给定名称的bean定义 (不为空 {@code null})
	 * @throws NoSuchBeanDefinitionException 没有对应的bean定义
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 检查此注册表是否包含具有给定名称的bean定义。
	 * @param beanName 要查找的bean定义对应的名称
	 * @return 是否包含指定名称对应的bean定义
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 注册器里所有bean定义的名称集合
	 * @return 注册器里所有bean定义的名称集合，没有则为空集合
	 */
	String[] getBeanDefinitionNames();

	/**
	 * 返回bean定义的数量
	 * @return bean定义的数量
	 */
	int getBeanDefinitionCount();

	/**
	 * 判断给定的名称是否被注册器使用
	 * 是否有bean或者别名使用此名称注册
	 * @param beanName 要检查的名称
	 * @return 判断给定的名称是否被注册器使用
	 */
	boolean isBeanNameInUse(String beanName);

}
