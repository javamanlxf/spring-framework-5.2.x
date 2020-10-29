/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.Iterator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.lang.Nullable;

/**
 * 大多数可列出bean的工厂都会实现这个配置接口。处了 {@link ConfigurableBeanFactory},
 * 它还提供了用于分析和修改Bean定义以及预先实例化单例的工具。
 *
 * <p>{@link org.springframework.beans.factory.BeanFactory}的这个子接口不能用于常规应用程序代码：
 * 坚持使用{@link org.springframework.beans.factory.BeanFactory} 或者
 * {@link org.springframework.beans.factory.ListableBeanFactory} 用于典型的用例。
 * 即使需要访问bean工厂配置方法，该接口也仅允许框架内部即插即用。
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory()
 */
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	/**
	 * 忽略给定的依赖类型进行自动装配：例如，字符串。 默认为无。
	 * @param type 要忽略的依赖类型
	 */
	void ignoreDependencyType(Class<?> type);

	/**
	 * 忽略给定的依赖类型进行自动装配
	 * <p>应用程序上下文通常会使用它来注册以其他方式解决依赖关系的bean，
	 * 例如通过BeanFactoryAware的BeanFactory或通过ApplicationContextAware的ApplicationContext。
	 * <p>默认情况下，仅BeanFactoryAware接口被忽略。 要忽略其他类型，请为每种类型调用此方法。
	 * @param ifc 要忽略的依赖接口
	 * @see org.springframework.beans.factory.BeanFactoryAware
	 * @see org.springframework.context.ApplicationContextAware
	 */
	void ignoreDependencyInterface(Class<?> ifc);

	/**
	 * 用相应的自动装配值注册一个特殊的依赖类型。
	 * <p>这个适用于未在工厂中定义未beans的工厂上下文引用：
	 * 例如：类型为ApplicationContext的依赖关系已解析为该bean所在的ApplicationContext实例。
	 * <p>注意：在普通BeanFactory中没有注册这样的默认类型，甚至BeanFactory接口本身也没有。
	 * @param dependencyType 要注册的依赖项类型。 这通常是一个基本接口，例如BeanFactory，
	 *                          并且只要声明为自动装配依赖项（例如ListableBeanFactory），
	 *                          并且给定值实现扩展接口。它的扩展名也可以解析。
	 * @param autowiredValue 匹配的自动注入值，可能实现{@link org.springframework.beans.factory.ObjectFactory}接口，
	 *                       允许对实际目标值的延迟解析
	 */
	void registerResolvableDependency(Class<?> dependencyType, @Nullable Object autowiredValue);

	/**
	 * 确定指定的bean是否符合自动装配候选条件，以注入到声明匹配类型依赖项的其他bean中。
	 * <p>此方法还检查祖先工厂。
	 * @param beanName 要检查的bean的名称
	 * @param descriptor 要解决的依赖项的描述符
	 * @return whether 是否应将bean视为自动装配候选对象
	 * @throws NoSuchBeanDefinitionException 没有给定名称的bean
	 */
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor)
			throws NoSuchBeanDefinitionException;

	/**
	 * 返回指定Bean的注册BeanDefinition，从而允许访问其属性值和构造函数参数值（可以在Bean工厂后处理期间进行修改）。
	 * <p>返回的BeanDefinition对象不应是副本，而应是工厂中注册的原始定义对象。
	 * 这意味着，如有必要，应将其转换为更具体的实现类型。
	 * <p><b>注意:</b> 此方法不考虑祖先工厂。 它仅用于访问该工厂的本地bean定义。
	 * @param beanName 相应bean的名称
	 * @return 注册的bean 定义
	 * @throws NoSuchBeanDefinitionException 没有给定名称的bean
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * 返回对此工厂管理的所有bean名称的统一视图。
	 * <p>包括Bean定义名称以及手动注册的单例实例的名称，
	 * 并且始终将Bean定义名称排在首位，类似于特定于类型/注解的Bean名称检索的工作方式。
	 * @return Bean名称视图的复合迭代器
	 * @since 4.1.2
	 * @see #containsBeanDefinition
	 * @see #registerSingleton
	 * @see #getBeanNamesForType
	 * @see #getBeanNamesForAnnotation
	 */
	Iterator<String> getBeanNamesIterator();

	/**
	 * 清除合并的bean定义缓存，删除尚未被认为适合完全元数据缓存的bean条目。
	 * <p>通常在更改原始bean定义后触发，例如 在应用{@link BeanFactoryPostProcessor}之后。
	 * 请注意，此时将保留已经创建的bean的元数据。
	 * @since 4.2
	 * @see #getBeanDefinition
	 * @see #getMergedBeanDefinition
	 */
	void clearMetadataCache();

	/**
	 * 冻结所有bean定义，表示已注册的bean定义将不会被进一步修改或后处理。
	 * <p>这允许工厂积极地缓存bean定义元数据。
	 */
	void freezeConfiguration();

	/**
	 * 返回此工厂的Bean定义是否冻结，即不应再进行任何修改或后期处理。
	 * @return {@code true} 如果工厂的配置被认为是冻结的
	 */
	boolean isConfigurationFrozen();

	/**
	 * 确保所有非延迟初始单例都实例化，同时考虑{@link org.springframework.beans.factory.FactoryBean FactoryBeans}，如果需要。
	 * 通常在工厂设置结束时调用。
	 * @throws BeansException 如果无法创建一个单例bean。
	 * 注意：这可能已经离开工厂，并且已经初始化了一些bean！在这种情况下，请调用{@link #destroySingletons（）}进行全面清理。
	 * @see #destroySingletons()
	 */
	void preInstantiateSingletons() throws BeansException;

}
