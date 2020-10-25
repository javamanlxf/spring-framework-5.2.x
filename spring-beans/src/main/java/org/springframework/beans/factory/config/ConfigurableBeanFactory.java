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

import java.beans.PropertyEditor;
import java.security.AccessControlContext;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;
import org.springframework.util.StringValueResolver;

/**
 * 大多数的Bean工厂都会实现这个配置接口.
 * 除了在{@link org.springframework.beans.factory.BeanFactory}中的方法外，
 * 提供了一种配置Bean工厂属性的机制。
 */

 /** 这个Bean工厂接口并不推荐在常规的程序代码中使用，在典型的需求中，
  * 我们仍应坚持使用{@link org.springframework.beans.factory.BeanFactory}
  * 或者{@link org.springframework.beans.factory.ListableBeanFactory}。
  * 这个扩展的接口只是为了在框架内部做到即插即用，并能够特殊访问Bean工厂的配置方法。
 */

/** @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.beans.factory.ListableBeanFactory
 * @see ConfigurableListableBeanFactory
 */
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	/**
	 * 定义一个标准的单例作用域（单例：通过spring容器获得该bean时总是返回唯一的实例）
	 * 可以通过registerScope添加自定义的作用域
	 * @see #registerScope
	 */
	String SCOPE_SINGLETON = "singleton";

	/**
	 * 定义一个标准的原型作用域（原型：表示每次获得bean都会生成一个新的对象）
	 * 可以通过registerScope添加自定义的作用域
	 * @see #registerScope
	 */
	String SCOPE_PROTOTYPE = "prototype";

	/**
	 * 设置Bean工厂的父类
	 * 注意父类不能更改（如果在工厂实例化时不可用，则只能在构造函数外部设置。）
	 * 如果该工厂已经有一个父工厂了，就抛出IllegalStateException异常
	 * @param parentBeanFactory
	 * @throws IllegalStateException
	 */
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	/**
	 * 设置加载bean类的类加载器
	 * 默认值为线程上下文类加载器。
	 * 注意，此类加载器仅适用于尚不包含已解析的bean类的bean定义。
	 * spring2.0之后，Bean的Definitions中就只包含类名了，在工厂处理的Bean的Definitions就没有这样的问题了
	 * @param beanClassLoader
	 */
	void setBeanClassLoader(@Nullable ClassLoader beanClassLoader);

	/**
	 * 为加载Bean类，去获取当前工厂的类加载器
	 * 只有当系统的类加载器都访问不到时才返回null
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 * @return
	 */
	@Nullable
	ClassLoader getBeanClassLoader();


	/**
	 * 指定一个临时的类加载器用于类型匹配
	 * 默认为空，仅使用标准bean ClassLoader。
	 * 如果涉及到加载时编织，通常只是指定一个临时的ClassLoader，
	 * 以确保尽可能延迟地加载实际的bean类。一旦BeanFactory完成其引导阶段，便将临时加载器删除。
	 *
	 * 类加载编织：在目标类的class文件被JVM加载前, 通过自定义类加载器或者类文件转换器将横切逻辑织入到目标类的class文件中,
	 * 然后将修改后class文件交给JVM加载。这种织入方式可以简称为LTW(LoadTimeWeaving)。实现AOP原理。
	 * @param tempClassLoader
	 */
	void setTempClassLoader(@Nullable ClassLoader tempClassLoader);

	/**
	 * 返回临时的类加载器（如果有）来做类型匹配
	 * @since 2.5
	 * @return
	 */
	@Nullable
	ClassLoader getTempClassLoader();


	/**
	 * 设置是否缓存bean元数据，例如给定的bean定义（以合并方式）和已解析的bean类缓存元数据。
	 * 此标志设为true可启用Bean定义对象，特别是Bean类的实时刷新。
	 * 反之，则任何bean实例的创建都将重新查询bean类加载器以获取新解析的类。
	 * @param cacheBeanMetadata
	 */
	void setCacheBeanMetadata(boolean cacheBeanMetadata);


	/**
	 * 返回是否缓存bean元数据，例如给定的bean定义以合并方式）和已解析的bean类。
	 * @return
	 */
	boolean isCacheBeanMetadata();


	/**
	 * 给bean定义的值中的表达式指定解析的策略
	 * 默认情况下，BeanFactory中不支持任何表达式。
	 * ApplicationContext通常会在此处设置标准的表达式策略，以统一EL兼容样式支持“＃{...}”表达式。
	 * @param resolver
	 */
	void setBeanExpressionResolver(@Nullable BeanExpressionResolver resolver);


	/**
	 * 返回bean定义值中表达式的解析策略
	 * @return
	 */
	@Nullable
	BeanExpressionResolver getBeanExpressionResolver();


	/**
	 * 指定一个Spring 3.0的ConversionService以用于转换属性值，
	 * 以替代JavaBeans PropertyEditors。
	 * @param conversionService
	 */
	void setConversionService(@Nullable ConversionService conversionService);


	/**
	 * 返回相关的ConversionService（如果有的话）
	 * @return
	 */
	@Nullable
	ConversionService getConversionService();


	/**
	 * 添加一个PropertyEditorRegistrar并应用于所有bean创建过程。
	 * 这样的registrar会创建新的PropertyEditor实例，并在给定的注册表中注册它们，每次创建bean使用的编辑器都是最新的。
	 * 这就避免了在自定义编辑器上进行同步的需要；因此，通常最好使用当前方法代替{@link #registerCustomEditor}方法
	 * @param registrar
	 */
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);


	/**
	 * 在出厂配置期间，为给定类型的所有属性注册给定的定制属性编辑器。
	 * 此方法将注册一个共享的自定义编辑器实例。 对该实例的访问将被同步以确保线程安全。
	 * 通常最好使用{@link #addPropertyEditorRegistrar}代替此方法，以避免在自定义编辑器上进行同步。
	 * @param requiredType
	 * @param propertyEditorClass
	 */
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);

	/**
	 * 用已在此BeanFactory中注册的自定义编辑器，来初始化给定的PropertyEditorRegistry
	 * @param registry PropertyEditorRegistry进行初始化
	 */
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);


	/**
	 * 设置此BeanFactory用于转换bean属性值，构造函数参数值等的 自定义类型转换器。
	 * 这将覆盖默认的PropertyEditor机制，使任何自定义编辑器或自定义编辑器registrars都不相关。
	 * @param typeConverter 类型转换器
	 * @since 2.5
	 * @see #addPropertyEditorRegistrar
	 * @see #registerCustomEditor
	 */
	void setTypeConverter(TypeConverter typeConverter);

	/**
	 * 获得此BeanFactory使用的类型转换器。
	 * 由于TypeConverters通常不是线程安全的，所以对于每次调用，这可能都会get一个新的实例。
	 * 如果默认的PropertyEditor机制处于活动状态，则返回的TypeConverter将知道已注册的所有自定义编辑器。
	 * @return
	 * @since 2.5
	 */
	TypeConverter getTypeConverter();


	/**
	 * 为嵌入值（例如注解属性）添加字符串解析器。
	 * @param valueResolver 用于嵌入值的字符串解析器
	 */

	void addEmbeddedValueResolver(StringValueResolver valueResolver);

	/**
	 *确定是否已通过{@link #resolveEmbeddedValue（String）}应用此bean工厂的嵌入式值解析器。
	 * @return
	 * @since 4.3
	 */
	boolean hasEmbeddedValueResolver();


	/**
	 * 解析给定的嵌入值，例如注解属性。
	 * @param value
	 * @return
	 */
	@Nullable
	String resolveEmbeddedValue(String value);


	/**
	 * 添加一个新的BeanPostProcessor，用于在工厂配置期间，用于该工厂创建的bean。
	 * 注意：此处提交的后处理器将按照注册的顺序应用；通过实现{@link org.springframework.core.Ordered}接口表示的任何排序语义都将被忽略。
	 * 请注意自动检测到的后处理器（例如，作为ApplicationContext中的bean）将始终以编程方式注册后的处理器。
	 * @param beanPostProcessor
	 */
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);


	/**
	 * 返回当前已注册的BeanPostProcessor的数量（如果有）。
	 * @return
	 */
	int getBeanPostProcessorCount();


	/**
	 * 通过注册由给定作用域，来实现支持给定的作用域。
	 * @param scopeName
	 * @param scope
	 */
	void registerScope(String scopeName, Scope scope);


	/**
	 * 返回当前所有的注册作用域名
	 * 这将仅返回显式注册的作用域的名称。 内置作用域（例如“ singleton”和“ prototype”）不会返回。
	 * @return
	 */
	String[] getRegisteredScopeNames();


	/**
	 * 返回给定作用域名称的作用域实现（如果有）。
	 * 这将仅返回显式注册的作用域的名称。 内置作用域（例如“ singleton”和“ prototype”）不会返回。
	 * @param scopeName
	 * @return
	 */
	@Nullable
	Scope getRegisteredScope(String scopeName);


	/**
	 * 提供与此工厂有关的安全访问控制上下文。
	 * @return
	 */
	AccessControlContext getAccessControlContext();


	/**
	 * 从给定的其他工厂复制所有相关配置。
	 * @param otherFactory
	 * 应该包括所有标准配置设置以及BeanPostProcessors，作用域，和工厂特定的内部设置。
	 * 不应包含任何实际bean定义的元数据，例如BeanDefinition对象和bean名称别名。
	 */
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);


	/**
	 * 给定一个bean名称，创建一个别名。我们通常使用此方法来支持XML的ID标签用于bean名称）中非法的名称。
	 * 通常在工厂配置期间调用，但也可以用于别名的运行时注册。因此，工厂的实现应同步别名访问。
	 * @param beanName
	 * @param alias
	 * @throws BeanDefinitionStoreException
	 */
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;


	/**
	 * 解析所有别名目标名称和在该工厂中注册的别名，将给定的StringValueResolver应用于它们。
	 * 值解析器可以解析例如目标bean名称中甚至别名中的占位符。
	 * @param valueResolver
	 */
	void resolveAliases(StringValueResolver valueResolver);


	/**
	 * 返回给定bean名称的合并BeanDefinition，如有必要，将子bean定义与其父级合并。
	 * @param beanName
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;


	/**
	 * 确定具有给定名称的Bean是否为FactoryBean。
	 * @param name
	 * @return
	 * @throws NoSuchBeanDefinitionException
	 */
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;


	/**
	 * 明确控制指定bean的当前增量状态。仅供内部容器使用。
	 * @param beanName
	 * @param inCreation
	 */
	void setCurrentlyInCreation(String beanName, boolean inCreation);


	/**
	 * 确定指定的bean当前是否正在创建中。
	 * @param beanName
	 * @return
	 */
	boolean isCurrentlyInCreation(String beanName);


	/**
	 * 为给定bean注册一个从属bean，并在给定bean被销毁之前被销毁。
	 * @param beanName
	 * @param dependentBeanName
	 */
	void registerDependentBean(String beanName, String dependentBeanName);


	/**
	 * 返回依赖于指定bean的所有bean的名称（如果有）。
	 * @param beanName
	 * @return
	 */
	String[] getDependentBeans(String beanName);


	/**
	 * 返回指定bean依赖的所有bean的名称（如果有）。
	 * @param beanName
	 * @return
	 */
	String[] getDependenciesForBean(String beanName);

	/**
	 * 根据其bean定义销毁给定的bean实例（通常是从该工厂获得的原型实例*）。
	 * 销毁过程中出现的任何异常都应被捕获并记录下来，而不是传给此方法的调用者。
	 * @param beanName
	 * @param beanInstance
	 */
	void destroyBean(String beanName, Object beanInstance);

	/**
	 * 销毁当前目标范围中的指定范围的bean（如果有）。
	 * 销毁过程中出现的任何异常都应被捕获并记录下来，而不是传给此方法的调用者。
	 * @param beanName
	 */
	void destroyScopedBean(String beanName);

	/**
	 * 销毁该工厂中的所有单例Bean，包括已注册为一次性的Bean。在工厂关闭时被调用。
	 * 销毁过程中出现的任何异常都应被捕获并记录下来，而不是传给此方法的调用者。
	 */
	void destroySingletons();

}
