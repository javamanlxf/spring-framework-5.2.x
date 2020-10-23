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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * 扩展{@link BeanFactory} 接口,提供所有bean 实例的枚举,不再需要客户端通过一个个bean name查找.
 * BeanFactory实现类预加载bean定义(如通过实现xml的工厂)需要实现这个接口.
 *
 * <p>如果一样实现了{@link HierarchicalBeanFactory},返回值不会考虑父类BeanFactory,
 * 只考虑当前factory定义的类.当然也可以使用{@link BeanFactoryUtils}辅助类来查找祖先工厂中的类.
 *
 * <p>T这个接口中的方法只会考虑本factory定义的bean.
 * 这些方法会忽略{@link org.springframework.beans.factory.config.ConfigurableBeanFactory}
 * 的{@code registerSingleton}注册的单例bean,{@code getBeanNamesOfType}和{@code getBeansOfType}是例外,
 * 一样会考虑手动注册的单例.当然BeanFactory的getBean一样可以透明访问这些特殊bean.
 * 当然在典型情况下,所有的bean都是由external bean定义,所以应用不需要顾虑这些差别.
 *
 * <p><b>注意:</b> {@code getBeanDefinitionCount}和{@code containsBeanDefinition}的实现方法因为效率比较低,并不是供频繁调用的.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16 April 2001
 * @see HierarchicalBeanFactory
 * @see BeanFactoryUtils
 */
public interface ListableBeanFactory extends BeanFactory {

	/**
	 * 检查bean factory是否含有给定name的bean定义.
	 * <p>不考虑该工厂可能参与的任何层次结构，
	 * 并且将忽略通过bean定义以外的其他方式注册的任何单例bean。
	 * @param beanName 要查找的bean的名称
	 * @return bean工厂是否包含所给的名称的bean
	 * @see #containsBean
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 返回bean工厂中定义的bean的数量
	 * <p>不考虑该工厂可能参与的任何层次结构，
	 * 并且将忽略通过bean定义以外的其他方式注册的任何单例bean。
	 * @return 返回bean工厂中定义的bean的数量
	 */
	int getBeanDefinitionCount();

	/**
	 * 返回工厂中定义的所有bean的名称
	 不考虑该工厂可能参与的任何层次结构，
	 * 并且将忽略通过bean定义以外的其他方式注册的任何单例bean。
	 * @return 工厂中定义的所有bean的名称
	 * 或者未定义返回空数组
	 */
	String[] getBeanDefinitionNames();

	/**
	 * 根据Factory Beans的bean定义或{@code getObjectType}的值判断，
	 * 返回与给定类型（包括子类）匹配的bean名称。
	 * <p><b>注意:这个方法仅检查顶级bean.</b> 它不会检查嵌套的bean.
	 * FactoryBean创建的bean会匹配为FactoryBean而不是原始类型.
	 * <p>是否考虑由FactoryBeans创建的对象，这意味着将初始化FactoryBeans。
	 * 如果由FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配
	 * <p>不会考虑该工厂参与的层次结构,可以使用BeanFactoryUtils中的
	 * {@code beanNamesForTypeIncludingAncestors}可以将祖先bean注册的单例纳入判断
	 * <p>注意: 不忽略通过其他方式生成的bean定义
	 * <p>这个版本的{@code getBeanNamesForType}会匹配所有类型的bean,包括单例,原型,FactoryBean.
	 * 在大多数实现中返回结果跟{@code getBeanNamesForType(type, true, true)}一样.
	 * 返回的bean names会根据backend 配置的进行排序.
	 * @param type 要匹配的通用类型的类或接口
	 * @return 返回匹配给定类型（包含字类）的beans（或者FactoryBean产生的对象）的名称，
	 * 如果没匹配返回空数组
	 * @since 4.2
	 * @see #isTypeMatch(String, ResolvableType)
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, ResolvableType)
	 */
	String[] getBeanNamesForType(ResolvableType type);

	/**
	 * 根据Factory Beans的bean定义或{@code getObjectType}的值判断，
	 * 返回与给定类型（包括子类）匹配的bean名称。
	 * <p><b>注意:这个方法仅检查顶级bean.</b> 它不会检查嵌套的bean.
	 * <p>是否考虑由FactoryBean创建的bean，如果设置了"allowEagerInit"标志,
	 * 那么FactoryBeans将会被初始化.如果由FactoryBean创建的bean不匹配, 
	 * 原生的FactoryBean会被匹配 如果"allowEagerInit"标志没有设置,
	 * 只要原生FactoryBean本身会被检查（不需要每个FactoryBean的初始化）
	 * <p>不会考虑该工厂参与的层次结构,可以使用BeanFactoryUtils中的
	 * @code beanNamesForTypeIncludingAncestors}可以将祖先bean注册的单例纳入判断
	 * <p>注意: 不忽略通过其他方式生成的bean定义
	 * <p>返回的bean names会根据backend 配置的进行排序.
	 * @param type 要匹配的通用类型的类或接口
	 * @param includeNonSingletons 是否也包含原型或范围内的bean还是仅包含单例（也适用于FactoryBeans）
	 * @param allowEagerInit 是否初始化lazy-init单例和由FactoryBeans（或带有“ factory-bean”引用的工厂方法） 
	 *                          创建的对象以进行类型检查。 请注意，需要急切初始化FactoryBeans以确定它们的类型：
	 *                          为此标志传递“ true”将初始化FactoryBeans和“ factory-bean”引用。
	 * @return 返回匹配给定类型（包含字类）的beans（或者FactoryBean产生的对象）的名称，
	 * 如果没匹配返回空数组
	 * @since 5.2
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, ResolvableType, boolean, boolean)
	 */
	String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 根据Factory Beans的bean定义或{@code getObjectType}的值判断，
	 * 返回与给定类型（包括子类）匹配的bean名称。
	 * <p><b>注意:这个方法仅检查顶级bean.</b> 它不会检查嵌套的bean.
	 * <p>是否考虑由FactoryBeans创建的对象，这意味着将初始化FactoryBeans。
	 * 如果由FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配
	 * <p>不会考虑该工厂参与的层次结构,可以使用BeanFactoryUtils中的
	 * @code beanNamesForTypeIncludingAncestors}可以将祖先bean注册的单例纳入判断
	 * <p>注意: 不忽略通过其他方式生成的bean定义
	 * <p>这个版本的{@code getBeanNamesForType}会匹配所有类型的bean,包括单例,原型,FactoryBean.
	 * 在大多数实现中返回结果跟{@code getBeanNamesForType(type, true, true)}一样.
	 * 返回的bean names会根据backend 配置的进行排序.
	 * @param type 要匹配bean的名称，{@code null} 查所有bean
	 * @return 返回匹配给定类型（包含字类）的beans（或者FactoryBean产生的对象）的名称，
	 * 如果没匹配返回空数组
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	String[] getBeanNamesForType(@Nullable Class<?> type);

	/**
	 * 根据Factory Beans的bean定义或{@code getObjectType}的值判断，
	 * 返回与给定类型（包括子类）匹配的bean名称。
	 * <p><b>注意:这个方法仅检查顶级bean.</b> 它不会检查嵌套的bean.
	 * <p>是否考虑由FactoryBean创建的bean，如果设置了"allowEagerInit"标志,
	 * 那么FactoryBeans将会被初始化.如果由FactoryBean创建的bean不匹配, 
	 * 原生的FactoryBean会被匹配 如果"allowEagerInit"标志没有设置,
	 * 只要原生FactoryBean本身会被检查（不需要每个FactoryBean的初始化）
	 * <p>不会考虑该工厂参与的层次结构,可以使用BeanFactoryUtils中的
	 * @code beanNamesForTypeIncludingAncestors}可以将祖先bean注册的单例纳入判断
	 * <p>注意: 不忽略通过其他方式生成的bean定义
	 * <p>在大多数实现中返回结果跟{@code getBeanNamesForType(type, true, true)}一样.
	 * 返回的bean names会根据backend 配置的进行排序.
	 * @param type 要匹配bean的名称，{@code null} 查所有bean
	 * @param includeNonSingletons 是否也包含原型或范围内的bean还是仅包含单例（也适用于FactoryBeans）
	 * @param allowEagerInit 是否初始化lazy-init单例和由FactoryBeans（或带有“ factory-bean”引用的工厂方法） 
	 *                          创建的对象以进行类型检查。 请注意，需要急切初始化FactoryBeans以确定它们的类型：
	 *                          为此标志传递“ true”将初始化FactoryBeans和“ factory-bean”引用。
	 * @return 返回匹配给定类型（包含字类）的beans（或者FactoryBean产生的对象）的名称，
	 * 如果没匹配返回空数组
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	String[] getBeanNamesForType(@Nullable Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);

	/**
	 * 根据Factory Beans的bean定义或{@code getObjectType}的值判断，
	 * 返回与给定类型（包括子类）匹配的bean名称。
	 * <p><b>注意:这个方法仅检查顶级bean.</b> 它不会检查嵌套的bean.
	 * <p>是否考虑由FactoryBeans创建的对象，这意味着将初始化FactoryBeans。
	 * 如果由FactoryBean创建的对象不匹配，则原始FactoryBean本身将与该类型匹配
	 * <p>不会考虑该工厂参与的层次结构,可以使用BeanFactoryUtils中的
	 * @code beanNamesForTypeIncludingAncestors}可以将祖先bean注册的单例纳入判断
	 * <p>注意: 不忽略通过其他方式生成的bean定义
	 * <p>这个版本的{@code getBeanNamesForType}会匹配所有类型的bean,包括单例,原型,FactoryBean.
	 * 在大多数实现中返回结果跟{@code getBeanNamesForType(type, true, true)}一样.
	 * 返回的bean names会根据backend 配置的进行排序.
	 * @param type 要匹配bean的名称，{@code null} 查所有bean
	 * @return 返回匹配bean的map，bean名称作为key，实例作为value
	 * @throws BeansException bean无法创建
	 * @since 1.1.2
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class)
	 */
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type) throws BeansException;

	/**
	 * 根据Factory Beans的bean定义或{@code getObjectType}的值判断，
	 * 返回与给定类型（包括子类）匹配的bean名称。
	 * <p><b>注意:这个方法仅检查顶级bean.</b> 它不会检查嵌套的bean.
	 * <p>是否考虑由FactoryBean创建的bean，如果设置了"allowEagerInit"标志,
	 * 那么FactoryBeans将会被初始化.如果由FactoryBean创建的bean不匹配, 
	 * 原生的FactoryBean会被匹配 如果"allowEagerInit"标志没有设置,
	 * 只要原生FactoryBean本身会被检查（不需要每个FactoryBean的初始化）
	 * <p>不会考虑该工厂参与的层次结构,可以使用BeanFactoryUtils中的
	 * @code beanNamesForTypeIncludingAncestors}可以将祖先bean注册的单例纳入判断
	 * <p>注意: 不忽略通过其他方式生成的bean定义
	 * <p>返回的bean名称和实例的map需要按照backend配置的顺序
	 * @param type 要匹配bean的名称，{@code null} 查所有bean
	 * @param includeNonSingletons 是否也包含原型或范围内的bean还是仅包含单例（也适用于FactoryBeans）
	 * @param allowEagerInit 是否初始化lazy-init单例和由FactoryBeans（或带有“ factory-bean”引用的工厂方法） 
	 *                          创建的对象以进行类型检查。 请注意，需要急切初始化FactoryBeans以确定它们的类型：
	 *                          为此标志传递“ true”将初始化FactoryBeans和“ factory-bean”引用。
	 * @return 返回匹配bean的map，bean名称作为key，实例作为value
	 * @throws BeansException bean无法创建
	 * @see FactoryBean#getObjectType
	 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(ListableBeanFactory, Class, boolean, boolean)
	 */
	<T> Map<String, T> getBeansOfType(@Nullable Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException;

	/**
	 * 查找所有使用提供的{@link Annotation}类型进行注释的bean名称，而无需创建相应的bean实例。
	 * <p>请注意，此方法考虑由FactoryBeans创建的对象，这意味着将初始化FactoryBeans以确定其对象类型。
	 * @param annotationType 要查找的注释的类型（在指定bean的类，接口或工厂方法级别）
	 * @return 匹配名称的bean
	 * @since 4.0
	 * @see #findAnnotationOnBean
	 */
	String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType);

	/**
	 * 查找所有使用提供的{@link Annotation}类型进行注释的bean名称，返回ben名称-实例的map
	 * <p>请注意，此方法考虑由FactoryBeans创建的对象，这意味着将初始化FactoryBeans以确定其对象类型。
	 * @param annotationType 要查找的注释的类型（在指定bean的类，接口或工厂方法级别）
	 * @return 返回匹配的map，bean名称做key，实例做value
	 * @throws BeansException bean无法创建
	 * @since 3.0
	 * @see #findAnnotationOnBean
	 */
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;

	/**
	 * 在指定的bean上找到{@code annotationType}的{@link Annotation}，
	 * 遍历其接口和超类（如果在给定类本身上找不到注解），并检查bean的工厂方法（如果有）。
	 * @param beanName 要查找的bean的名称 annotations on
	 * @param annotationType 要查找的注释的类型（在指定bean的类，接口或工厂方法级别）
	 * @return bean的注解，如果没有返回null
	 * @throws NoSuchBeanDefinitionException 没有指定的bean
	 * @since 3.0
	 * @see #getBeanNamesForAnnotation
	 * @see #getBeansWithAnnotation
	 */
	@Nullable
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException;

}
