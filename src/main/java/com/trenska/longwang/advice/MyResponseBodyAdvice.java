package com.trenska.longwang.advice;

/**
 * 2019/9/17
 * 创建人:Owen
 */
//@ControllerAdvice
//public class MyResponseBodyAdvice implements ResponseBodyAdvice<Object> {
//
//	@Override
//	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//		System.out.println("MyResponseBodyAdvice print returnType : " + returnType);
//		return true;
//	}
//
//	@Override
//	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
//		System.out.println("MyResponseBodyAdvice print body : "+body);
//		return body;
//	}
//}