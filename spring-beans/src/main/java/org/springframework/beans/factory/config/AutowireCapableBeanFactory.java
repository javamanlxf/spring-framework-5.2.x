/*
 * Copyright 2002-2019 the original author or authors.
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

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.lang.Nullable;

/**
 * {@link org.springframework.beans.factory.BeanFactory}接口的扩展将由能够自动装配的bean工厂实现，
 * 前提是它们希望为现有的bean实例公开此功能。
 *
 * <p>BeanFactory的此子接口不能在常规应用程序代码中使用：
 * 在典型的使用情况下，请坚持使用{@link org.springframework.beans.factory.BeanFactory}
 * 或{@link org.springframework.beans.factory.ListableBeanFactory}。
 *
 * <p>其他框架的集成代码可以利用此接口来连接和填充Spring无法控制其生命周期的现有bean实例。
 * 例如，这对于WebWork操作和Tapestry页面对象特别有用。
 *
 * <p>请注意，{@link org.springframework.context.ApplicationContext}
 * 外观未实现此接口，因为应用程序代码几乎从未使用过此接口。
 * 也就是说，它也可以从应用程序上下文中获得，可以通过ApplicationContext的{
 * @link org.springframework.context.ApplicationContext＃getAutowireCapableBeanFactory（）}方法进行访问。
 *
 * <p>
 * 296/5000
 * 您还可以实现{@link org.springframework.beans.factory.BeanFactoryAware}接口，
 * 该接口甚至在ApplicationContext中运行时也公开内部BeanFactory，以访问AutowireCapableBeanFactory：
 * 只需将传入的BeanFactory强制转换为AutowireCapableBeanFactory。
 *
 * @author Juergen Hoeller
 * @since 04.12.2003
 * @see org.springframework.beans.factory.BeanFactoryAware
 * @see org.springframework.beans.factory.config.ConfigurableListableBeanFactory
 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
 */
public interface AutowireCapableBeanFactory extends BeanFactory {

	/**
	 * 工厂没有自动装配的Bean。请注意，仍将应用BeanFactoryAware等和注释驱动的注入。
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_NO = 0;

	/**
	 * 表明根据名称自动装配(应用于所有bean属性setter)
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_NAME = 1;

	/**
	 * 表明根据类型自动装配(应用于所有bean属性setter)
	 * @see #createBean
	 * @see #autowire
	 * @see #autowireBeanProperties
	 */
	int AUTOWIRE_BY_TYPE = 2;

	/**
	 * 通过构造方法注入（涉及解析适当的构造函数）。
	 * @see #createBean
	 * @see #autowire
	 */
	int AUTOWIRE_CONSTRUCTOR = 3;

	/**
	 * 表明通过Bean的class的内部来自动装配 Spring3.0被弃用。
	 * @see #createBean
	 * @see #autowire
	 * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
	 * prefer annotation-based autowiring for clearer demarcation of autowiring needs.
	 */
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;

	/**
	 * 用于没有代理时，也能返回实例
	 * @since 5.1
	 * @see #initializeBean(Object, String)
	 * @see #applyBeanPostProcessorsBeforeInitialization(Object, String)
	 * @see #applyBeanPostProcessorsAfterInitialization(Object, String)
	 */
	String ORIGINAL_INSTANCE_SUFFIX = ".ORIGINAL";


	//-------------------------------------------------------------------------
	// 创建和填充外部Bean实例的典型方法
	//-------------------------------------------------------------------------

	/**
	 * 完全创建给定类的新bean实例。
	 * 执行Bean的完全初始化，包括所有适用的{@link BeanPostProcessor BeanPostProcessors}。
	 * 注意：这是用于创建一个新的实例，填充带注释的字段和方法，以及应用所有标准的bean初始化回调。 
	 * 这并不意味着传统的按属性的按名称或按类型自动装配将{@link #createBean（Class，int，boolean）}用于这些目的。
	 * @param beanClass 要创建的bean的class类型
	 * @return 新创建的bean的实例
	 * @throws BeansException 如果实例化或连接失败
	 */
	<T> T createBean(Class<T> beanClass) throws BeansException;

	/**
	 * 通过应用实例化后的回调和bean属性的后处理（例如注释驱动的注入）来填充给定的bean实例。
	 * 注意：这本质上是用于（重新）填充带注释的字段和方法，无论是用于新实例还是反序列化实例。
	 * 它并不意味着传统的按名称或按类型自动装配属性；
	 * 将{@link #autowireBeanProperties}用于这些目的。
	 * @param existingBean 已存在的bean的实例
	 * @throws BeansException 如果连接失败
	 */
	void autowireBean(Object existingBean) throws BeansException;

	/**
	 * 配置给定的原始bean：自动装配bean属性，应用bean属性值，
	 * 应用工厂回调（例如{@code setBeanName}和{@code setBeanFactory}），
	 * 以及应用所有bean后处理器（包括可能包装给定原始bean的处理器）。 。
	 * 这实际上是{@link #initializeBean}提供的超集，完全应用了由相应bean定义指定的配置。
	 * 注意：此方法需要给定名称的bean定义！
	 * @param existingBean 已存在的bean实例
	 * @param beanName bean的名称如有有必要请填写（这个名称对应的bean定义要存在）
	 * @return 要使用的bean实例，无论是原始的还是包装的
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * 没有对应名称的bean定义
	 * @throws BeansException 初始化失败
	 * @see #initializeBean
	 */
	Object configureBean(Object existingBean, String beanName) throws BeansException;


	//-------------------------------------------------------------------------
	// 专门的方法可以细粒度地控制bean的生命周期
	//-------------------------------------------------------------------------

	/**
	 * 使用指定的自动装配策略完全创建给定类的新bean实例。
	 * 此处支持此接口中定义的所有常量。对bean进行完全初始化，
	 * 包括所有适用的{@link BeanPostProcessor BeanPostProcessors}。
	 * 这实际上是{@link #autowire}提供的功能的超集，增加了{@link #initializeBean}行为。
	 * @param beanClass 要创建的bean的class类型
	 * @param autowireMode 通过名称或类型，使用此接口中的常量
	 * @param dependencyCheck 是否对对象执行依赖检查（不适用于自动装配构造函数，因此在那里被忽略）
	 * @return 新创建的bean的实例
	 * @throws BeansException 如果实例化或连接失败
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 */
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 使用指定的自动装配策略实例化给定类的新bean实例。 这里支持此接口中定义的所有常量。
	 * 也可以使用{@code AUTOWIRE_NO}进行调用，以便仅应用实例化之前的回调（例如，用于注释驱动的注入）。
	 * 不应用标准的{@link BeanPostProcessor BeanPostProcessors}回调或对bean进行任何进一步的初始化。 
	 * 该接口为此提供了独特的细粒度操作，例如{@link #initializeBean}。 
	 * 但是，如果适用于实例的构造，则将应用{@link InstantiationAwareBeanPostProcessor}回调。
	 * @param beanClass 要初始化的bean的class类型
	 * @param autowireMode 通过名称或类型，使用此接口中的常量
	 * @param dependencyCheck 是否对对象执行依赖检查（不适用于自动装配构造函数，因此在那里被忽略）
	 * @return 新创建的bean的实例
	 * @throws BeansException 如果实例化或连接失败
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #initializeBean
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;

	/**
	 * 按名称或类型自动装配给定bean实例的bean属性。
	 * 也可以使用{@code AUTOWIRE_NO}进行调用，以仅应用实例化后的回调（例如，用于注释驱动的注入）。
	 * 不应用标准的{@link BeanPostProcessor BeanPostProcessors}回调或对bean进行任何进一步的初始化。
	 * 该接口为此提供了独特的细粒度操作，例如{@link #initializeBean}。
	 * 但是，如果适用于实例的配置，则将应用{@link InstantiationAwareBeanPostProcessor}回调。
	 * @param existingBean 已存在的bean实例
	 * @param autowireMode 通过名称或类型，使用此接口中的常量
	 * @param dependencyCheck 是否对对象执行依赖检查
	 * @throws BeansException 连接失败
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_NO
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck)
			throws BeansException;

	/**
	 * 将具有给定名称的bean定义的属性值应用于给定bean实例。
	 * Bean定义可以定义一个完全自包含的Bean，重用其属性值，也可以仅定义要用于现有Bean实例的属性值。
	 * 此方法不自动装配bean属性； 它仅应用显式定义的属性值。 使用{@link #autowireBeanProperties}方法自动连接现有的bean实例。
	 * 注意：此方法需要给定名称的bean定义！ 不应用标准的{@link BeanPostProcessor BeanPostProcessors}回调或对bean进行任何进一步的初始化。
	 * 该接口为此提供了独特的细粒度操作，例如{@link #initializeBean}。
	 * 但是，如果适用于实例的配置，则将应用{@link InstantiationAwareBeanPostProcessor}回调。
	 * @param existingBean 已存在的bean实例
	 * @param beanName bean的名称如有有必要请填写（这个名称对应的bean定义要存在）
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * 没有有给定bean名称的bean定义
	 * @throws BeansException 应用属性值失败
	 * @see #autowireBeanProperties
	 */
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	/**
	 * 初始化给定的原始bean，应用工厂回调，例如{@code setBeanName}和{@code setBeanFactory}，
	 * 还应用所有bean后处理器（包括可能包装给定原始bean的处理器）。
	 * 请注意，bean工厂中不必存在给定名称的bean定义。
	 * 传入的Bean名称将仅用于回调，但不会根据已注册的Bean定义进行检查。
	 * @param existingBean 已存在的bean实例
	 * @param beanName Bean的名称，必要时将传递给它（仅传递给{@link BeanPostProcessor BeanPostProcessors}；
	 *                    可以遵循{@link #ORIGINAL_INSTANCE_SUFFIX}约定以强制返回给定的实例，即没有代理等）
	 * @return 要使用的bean实例，无论是原始的还是包装的
	 * @throws BeansException 初始化失败
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 */
	Object initializeBean(Object existingBean, String beanName) throws BeansException;

	/**
	 * 将{@link BeanPostProcessor BeanPostProcessors}应用于给定的现有bean实例，
	 * 调用其{@code postProcessBeforeInitialization}方法。 
	 * 返回的Bean实例可能是原始实例的包装。
	 * @param existingBean 已存在的bean实例
	 * @param beanName Bean的名称，必要时将传递给它（仅传递给{@link BeanPostProcessor BeanPostProcessors}；
	 *                    可以遵循{@link #ORIGINAL_INSTANCE_SUFFIX}约定以强制返回给定的实例，即没有代理等）
	 * @return 要使用的bean实例，无论是原始的还是包装的
	 * @throws BeansException 如果任何后处理失败
	 * @see BeanPostProcessor#postProcessBeforeInitialization
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 */
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 将{@link BeanPostProcessor BeanPostProcessors}应用于给定的现有bean实例，
	 * 调用其{@code postProcessAfterInitialization}方法。 返回的Bean实例可能是原始实例的包装。
	 * @param existingBean 已存在的bean实例
	 * @param beanName Bean的名称，必要时将传递给它（仅传递给{@link BeanPostProcessor BeanPostProcessors}；
	 *                    可以遵循{@link #ORIGINAL_INSTANCE_SUFFIX}约定以强制返回给定的实例，即没有代理等）
	 * @return 要使用的bean实例，无论是原始的还是包装的
	 * @throws BeansException 如果任何后处理失败
	 * @see BeanPostProcessor#postProcessAfterInitialization
	 * @see #ORIGINAL_INSTANCE_SUFFIX
	 */
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException;

	/**
	 * 使用{@link org.springframework.beans.factory.DisposableBean}协定
	 * 以及已注册的{@link DestructionAwareBeanPostProcessor DestructionAwareBeanPostProcessors}，
	 * 销毁给定的bean实例（通常来自{@link #createBean}）。
	 * 销毁期间发生的任何异常都应捕获并记录，而不是传播给此方法的调用方。
	 * @param existingBean the bean instance to destroy
	 */
	void destroyBean(Object existingBean);


	//-------------------------------------------------------------------------
	// 解析注入点的委托方法
	//-------------------------------------------------------------------------

	/**
	 * 解析与给定对象类型（如果有）唯一匹配的bean实例，包括其bean名称。
	 * 这实际上是{@link #getBean（Class）}的变体，保留了匹配实例的bean名称。
	 * @param requiredType 键入bean必须匹配； 可以是接口或超类
	 * @return Bean名称加Bean实例
	 * @throws NoSuchBeanDefinitionException 如果没有找到匹配的bean
	 * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的bean
	 * @throws BeansException 如果无法创建该bean
	 * @since 4.3.3
	 * @see #getBean(Class)
	 */
	<T> NamedBeanHolder<T> resolveNamedBean(Class<T> requiredType) throws BeansException;

	/**
	 * 为给定的bean名称解析一个bean实例，提供一个依赖描述符以暴露给目标工厂方法。
	 * 这实际上是{@link #getBean（String，Class）}的变体，
	 * 它使用{@link org.springframework.beans.factory.InjectionPoint}参数支持工厂方法。
	 * @param name 要查找的bean的名称
	 * @param descriptor 请求注入点的依赖项描述符
	 * @return 相应的bean实例
	 * @throws NoSuchBeanDefinitionException 如果没有指定名称的bean
	 * @throws BeansException 如果无法创建该bean
	 * @since 5.1.5
	 * @see #getBean(String, Class)
	 */
	Object resolveBeanByName(String name, DependencyDescriptor descriptor) throws BeansException;

	/**
	 * 解决针对此工厂中定义的Bean的指定依赖关系。
	 * @param descriptor 依赖项的描述符（字段/方法/构造函数）
	 * @param requestingBeanName 声明给定依赖项的bean的名称
	 * @return 解析的对象；如果找不到，则为{@code null}
	 * @throws NoSuchBeanDefinitionException 如果没有找到匹配的bean
	 * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的bean
	 * @throws BeansException 如果依赖项解析由于任何其他原因而失败
	 * @since 2.5
	 * @see #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)
	 */
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName) throws BeansException;

	/**
	 * 解决针对此工厂中定义的Bean的指定依赖关系。
	 * @param descriptor 依赖项的描述符（字段/方法/构造函数）
	 * @param requestingBeanName 声明给定依赖项的bean的名称
	 * @param autowiredBeanNames 一个集合，所有自动装配的bean的名称（用于解决给定的依赖性）都应该添加到其中
	 * @param typeConverter 用于填充数组和集合的TypeConverter
	 * @return 解析的对象；如果找不到，则为{@code null}
	 * @throws NoSuchBeanDefinitionException 如果没有找到匹配的bean
	 * @throws NoUniqueBeanDefinitionException 如果找到多个匹配的bean
	 * @throws BeansException 如果依赖项解析由于任何其他原因而失败
	 * @since 2.5
	 * @see DependencyDescriptor
	 */
	@Nullable
	Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
			@Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException;

}
