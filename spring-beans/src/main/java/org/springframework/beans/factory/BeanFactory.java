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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 访问一个Spring bean容器的根接口。
 *
 * <p>这是bean容器的基本组件;
 * 进一步的实现例如 {@link ListableBeanFactory} 和
 * {@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * 可以对BeanFactory做进一步的拓展
 *
 * <p>此接口由持有一些bean定义的对象来实现，每个bean由String字符串唯一标识。根据bean定义，
 * 工厂将返回一个独立对象实例（原型设计模式），或者一个单个共享实例（一个优秀的单例设计模式的替代品，
 * 其中该实例是一个factory范围内的单例）。实例的哪种类型将被返回依赖于bean工厂配置：即使API是一样的。
 * 从Spring2.0开始，作用域扩展到根据具体的应用上下文,如web环境的request,session。
 *
 * <p>这种方案的关键是，BeanFactory的是应用程序组件注册的中心,
 * 同时集中应用程序组件的配置（程序模块不再需要读取诸如properties的配置文件）。
 * 这种设计的更多好处讨论详见的<J2EE设计开发编程指南>第4和第11章.
 *
 * <p>相比诸如 BeanFactory 中查找的pull配置方式,
 * 通过setters或者构造方法,依赖注入的push方式配置应用对象更好.
 * Spring的依赖注入功能就是通过实现BeanFactory和其子接口实现的.
 *
 * <p>通常，一个BeanFactory会从配置源（如X​​ML文件）中加载bena定义,
 * 并使用 {@code org.springframework.beans} 包配置bean。但是，一个实现可以直接简单地返回
 * 在java代码里创建的java对象。存储bean定义的方式并没有限制，可以用 LDAP, RDBMS, XML,
 * properties文件等等。实现类都可以支持beans间的依赖（依赖注入）
 *
 * <p>与 {@link ListableBeanFactory} 中的方法不同, {@link HierarchicalBeanFactory}
 * 所有接口中的所有操作也会检查父工厂.如果在这个工厂实例找不到bean，去直接父工厂查找。
 * factory实例中的bean会覆盖父factory实例中的同名bean。
 *
 * <p>bean factory实现类应该尽量支持标准bean的生命周期接口，全套的初始化方法,排序如下：
 * <ol>
 * <li>BeanNameAware's {@code setBeanName}
 * <li>BeanClassLoaderAware's {@code setBeanClassLoader}
 * <li>BeanFactoryAware's {@code setBeanFactory}
 * <li>EnvironmentAware's {@code setEnvironment}
 * <li>EmbeddedValueResolverAware's {@code setEmbeddedValueResolver}
 * <li>ResourceLoaderAware's {@code setResourceLoader}
 * (只适用于在应用程序运行时上下文)
 * <li>ApplicationEventPublisherAware's {@code setApplicationEventPublisher}
 * (只适用于在应用程序运行时上下文)
 * <li>MessageSourceAware's {@code setMessageSource}
 * (只适用于在应用程序运行时上下文)
 * <li>ApplicationContextAware's {@code setApplicationContext}
 * (只适用于在应用程序运行时上下文)
 * <li>ServletContextAware's {@code setServletContext}
 * (只适用在web应用程序中运行时上下文)
 * <li>{@code postProcessBeforeInitialization} methods of BeanPostProcessors
 * <li>InitializingBean's {@code afterPropertiesSet}
 * <li>用户自定义的init-method方法  {@code @Bean（initMethod="init") }
 * <li>{@code postProcessAfterInitialization} methods of BeanPostProcessors
 * </ol>
 *
 * <p>关闭bean factory，声名周期方法如下：
 * <ol>
 * <li>{@code postProcessBeforeDestruction} methods of DestructionAwareBeanPostProcessors
 * <li>DisposableBean's {@code destroy}
 * <li>用户自定义的destroy-method方法  {@code @Bean（destroyMethod="destroy") }
 * </ol>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 13 April 2001
 * @see BeanNameAware#setBeanName
 * @see BeanClassLoaderAware#setBeanClassLoader
 * @see BeanFactoryAware#setBeanFactory
 * @see org.springframework.context.ResourceLoaderAware#setResourceLoader
 * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher
 * @see org.springframework.context.MessageSourceAware#setMessageSource
 * @see org.springframework.context.ApplicationContextAware#setApplicationContext
 * @see org.springframework.web.context.ServletContextAware#setServletContext
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization
 * @see InitializingBean#afterPropertiesSet
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization
 * @see DisposableBean#destroy
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName
 */
public interface BeanFactory {

	/**
	 * 间接引用一个 {@link FactoryBean} 的实例， 并且辨别由FactoryBean创建
	 * bean以&开头（{@code &myJndiObject}）表示获取FactoryBean实例.否则（{@code myJndiObject}）获取created的实例。
	 */
	String FACTORY_BEAN_PREFIX = "&";


	/**
	 * 返回一个实例，该实例可以是指定bean的共享或独立的。
	 * <p>此方法允许使用Spring BeanFactory代替单例或原型设计模式
	 * 对于Singleton bean，调用者可以保留对返回对象的引用。
	 * <p>将别名转换回相应的规范bean名称。
	 * <p>将询问父工厂是否在该工厂实例中找不到该bean。
	 * @param name 要检索的bean的名称
	 * @return Bean的一个实例
	 * @throws NoSuchBeanDefinitionException 不存在beanDefinition的异常
	 * @throws BeansException 如果这个bean无法获取到
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * 返回一个实例，该实例可以是指定bean的共享或独立的。
	 * <p>行为与 {@link #getBean(String)} 相同, 但是通过抛出BeanNotOfRequiredTypeException异常
	 * （是否这个bean没有必要的类型），提供了一种安全的方法。这意味着ClassCastException不能在强制转换时抛出，
	 * 但是可能在{@link #getBean(String)}里抛出.
	 * <p>将别名转换回相应的规范bean名称。
	 * <p>将询问父工厂是否在该工厂实例中找不到该bean。
	 * @param name 要检索的bean的名称
	 * @param requiredType bean所需的类型，可以是接口或超类
	 * @return Bean的一个实例
	 * @throws NoSuchBeanDefinitionException 不存在beanDefinition的异常
	 * @throws BeanNotOfRequiredTypeException 没有requireType的异常
	 * @throws BeansException 无法创建bean
	 */
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	/**
	 * 返回一个实例，该实例可以是指定bean的共享或独立的。
	 * <p>允许指定显式构造方法参数/工厂方法参数,
	 * 覆盖Bean定义中指定的默认参数（如果有）
	 * @param name 要检索的bean的名称
	 * @param args 使用显式参数创建Bean实例时要使用的参数
	 * (仅在创建新实例而不是检索现有实例时适用)
	 * @return Bean的一个实例
	 * @throws NoSuchBeanDefinitionException 不存在beanDefinition的异常
	 * @throws BeanDefinitionStoreException  如果给出了参数，但是受影响的bean不是原型
	 * @throws BeansException 无法创建bean
	 * @since 2.5
	 */
	Object getBean(String name, Object... args) throws BeansException;

	/**
	 * 返回与给定对象类型唯一匹配的bean实例（如果有）。
	 * <p>参考 {@link ListableBeanFactory} 里的by-type按类型查找
	 * 但也可以根据给定类型的名称转换为常规的按名称查找by-name。更多beans的操作可以使用
	 * {@link ListableBeanFactory} 和 {@link BeanFactoryUtils}.
	 * @param requiredType bean所需的类型，可以是接口或超类
	 * @return Bean的一个实例
	 * @throws NoSuchBeanDefinitionException 不存在beanDefinition的异常
	 * @throws NoUniqueBeanDefinitionException 给定的type匹配到不止一个bean
	 * @throws BeansException 无法创建bean
	 * @since 3.0
	 * @see ListableBeanFactory
	 */
	<T> T getBean(Class<T> requiredType) throws BeansException;

	/**
	 * 返回与给定对象类型唯一匹配的bean实例（如果有）。
	 * <p>允许指定显式构造方法参数/工厂方法参数,
	 * 覆盖Bean定义中指定的默认参数（如果有）
	 * <p>参考 {@link ListableBeanFactory} 里的by-type按类型查找
	 * 但也可以根据给定类型的名称转换为常规的按名称查找by-name。更多beans的操作可以使用
	 * {@link ListableBeanFactory} 和 {@link BeanFactoryUtils}.
	 * @param requiredType bean所需的类型，可以是接口或超类
	 * @param args 使用显式参数创建Bean实例时要使用的参数
	 * (仅在创建新实例而不是检索现有实例时适用)
	 * @return Bean的一个实例
	 * @throws NoSuchBeanDefinitionException 不存在beanDefinition的异常
	 * @throws BeanDefinitionStoreException 给定参数了，但是受影响的bean不是原型
	 * @throws BeansException 无法创建bean
	 * @since 4.1
	 */
	<T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

	/**
	 * 返回指定bean的提供程序，从而允许按需的实例延迟检索，包括可用性和唯一性选项。
	 * @param requiredType bean所需的类型，可以是接口或超类
	 * @return 指向Provider的句柄
	 * @since 5.1
	 * @see #getBeanProvider(ResolvableType)
	 */
	<T> ObjectProvider<T> getBeanProvider(Class<T> requiredType);

	/**
	 * 返回指定bean的提供程序，从而允许按需的实例延迟检索，包括可用性和唯一性选项。
	 * @param requiredType bean所需的类型，可以是通用类型声明。
	 * 支持反射性注入点，不支持集合类型. 以编程的方式检索指定类型的bean列表，指定实际的bean类型作为参数，然后使用
	 * {@link ObjectProvider#orderedStream()} 或者他的layz stream/iterator 选项
	 * @return 指向Provider的句柄
	 * @since 5.1
	 * @see ObjectProvider#iterator()
	 * @see ObjectProvider#stream()
	 * @see ObjectProvider#orderedStream()
	 */
	<T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType);

	/**
	 * 判断这个BeanFactory是否包含指定名字的bean定义或者是已注册包含指定名字的单例bean实例。
	 * <p>如果所给名字为别名,将会被转换回正确的规范的bean名字。
	 * <p>如果当前BeanFactory是分层的,那么当在本实例范围内找不到时会在其所有父工厂实例里查找。
	 * <p>I如果存在指定名字的bean定义或者是单例实例,返回 {@code true}
	 * 与抽象类还是实现类,延迟加载还是马上加载,当前范围还是其父工厂范围无关。
	 * 所以注意:这个方法返回{@code true}，但并不意味着当传入同样名字调用此工厂的
	 * {@link #getBean}方法时一定会获得对象实例。
	 * @param name 需要查找的bean的名称，可以是别名
	 * @return 是否包含bean
	 */
	boolean containsBean(String name);

	/**
	 * 判断是否是一个共享的单例对象。如果是,查看 {@link #getBean}方法看其是否一定会返回同一实例？
	 * <p>注意:此方法返回 {@code false}并不表示给定名字的实例一定是独立的实例。
	 * 它表示非单例实例,也可能是对应一定范围的Bean(request,session)。
	 * 使用 {@link #isPrototype} 方法判断是否是独立的实例
	 * <p>将别名转换回对应的规范的bean名字
	 * <p>当在本工厂实例中无法找到给定名字的bean时,在其父工厂中查找
	 * @param name 需要查找的bean的名称，可以是别名
	 * @return 这个bean是否是一个单例
	 * @throws NoSuchBeanDefinitionException 是否没有这个bean
	 * @see #getBean
	 * @see #isPrototype
	 */
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 判断是否是一个独立的对象,如果是true,如果是,查看 {@link #getBean} 是否一定会返回一个独立的实例？
	 * <p>注意:此方法返回 {@code false} 时,并不表示给定名字的实例一定是单例实例。
	 * 它表示非独立的实例,也可能是对应一定范围的Bean(request,session)。
	 * 使用 {@link #isSingleton} 判断是否是共享的单例实列
	 * <p>将别名转换回对应的规范的bean名字
	 * <p>当在本工厂实例中无法找到给定名字的bean时,在其父工厂中查找
	 * @param name 查询的bean的name
	 * @return 这个bean是否产生独立实例
	 * @throws NoSuchBeanDefinitionException 是否没有这个bean
	 * @since 2.0.3
	 * @see #getBean
	 * @see #isSingleton
	 */
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 检查给定名字的实例是否匹配指定类型。
	 * 具体的说,检查通过给定名字的一个 {@link #getBean}调用返回的对象是否是指定的目录类型。
	 * <p>将别名转换回对应的规范的bean名字
	 * <p>当在本工厂实例中无法找到给定名字的bean时,在其父工厂中查找
	 * @param name 查询的bean的name
	 * @param typeToMatch 要匹配的类型 (as a {@code ResolvableType})
	 * @return {@code true} 匹配这个类型
	 * {@code false} 不匹配这个类型
	 * @throws NoSuchBeanDefinitionException 是否没有这个bean
	 * @since 4.2
	 * @see #getBean
	 * @see #getType
	 */
	boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * 检查给定名字的实例是否匹配指定类型。
	 * 具体的说,检查通过给定名字的一个 {@link #getBean}调用返回的对象是否是指定的目录类型。
	 * <p>将别名转换回对应的规范的bean名字
	 * <p>当在本工厂实例中无法找到给定名字的bean时,在其父工厂中查找
	 * @param name 查询的bean的name
	 * @param typeToMatch 要匹配的类型 (as a {@code Class})
	 * @return {@code true} 匹配这个类型
	 * {@code false} 不匹配这个类型
	 * @throws NoSuchBeanDefinitionException 是否没有这个bean
	 * @since 2.0.1
	 * @see #getBean
	 * @see #getType
	 */
	boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

	/**
	 * 明确给定名字对应Bean的类型。
	 * 具体说就是,确定通过给定名字调用 {@link #getBean}方法返回的Object的类型。
	 * <p>对于 {@link FactoryBean}, 回FactoryBean创建的Object的类型,就像{@link FactoryBean#getObjectType()}方法
	 * 可能导致未初始化的开始初始化 {@code FactoryBean} (see {@link #getType(String, boolean)}).
	 * <p>将别名转换回对应的规范的bean名字
	 * <p>当在本工厂实例中无法找到给定名字的bean时,在其父工厂中查找
	 * @param name 查询的bean的name
	 * @return bean的类型，或者 {@code null}
	 * @throws NoSuchBeanDefinitionException 是否没有这个bean
	 * @since 1.1.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	@Nullable
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;

	/**
	 * 明确给定名字对应Bean的类型。
	 * 具体说就是,确定通过给定名字调用 {@link #getBean}方法返回的Object的类型。
	 * <p>对于 {@link FactoryBean}, 回FactoryBean创建的Object的类型,就像{@link FactoryBean#getObjectType()}方法
	 * {@code allowFactoryBeanInit} flag，会决定是否会导致未初始化的开始初始化
	 * {@code FactoryBean} (see {@link #getType(String, boolean)})
	 * <p>将别名转换回对应的规范的bean名字
	 * <p>当在本工厂实例中无法找到给定名字的bean时,在其父工厂中查找
	 * @param name 查询的bean的name
	 * @param allowFactoryBeanInit {@code FactoryBean} 是否可能被初始化
	 * 获取object的类型
	 * @return bean的类型，或者 {@code null}
	 * @throws NoSuchBeanDefinitionException 是否没有这个bean
	 * @since 5.2
	 * @see #getBean
	 * @see #isTypeMatch
	 */
	@Nullable
	Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException;

	/**
	 * 返回给定bean名称的别名（如果有）。
	 * <p>当在{@link #getBean}调用中使用时，所有这些别名都指向同一个bean。
	 * <p>如果给定名称是别名，则将返回相应的原始bean名称和其他别名（如果有），
	 * 原始bean名称是数组中的第一个元素。
	 * <p>将询问父工厂是否在该工厂实例中找不到该bean。
	 * @param name 用来检查别名的bean名称
	 * @return 别名，如果没有则为空数组
	 * @see #getBean
	 */
	String[] getAliases(String name);

}
