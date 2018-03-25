package unittest.mockito;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Parameter;

import static org.mockito.Mockito.mock;

/**
 * TODO: remove this extension class when mockito supports junit5 (potentially with 2.17.0 within the next days).
 * <p>
 * This code is provided by the JUnit5 team.
 */
public class MockitoExtension implements TestInstancePostProcessor, ParameterResolver {
	
	@Override
	public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) {
		MockitoAnnotations.initMocks(testInstance);
	}
	
	@Override
	public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
		return parameterContext.getParameter().isAnnotationPresent(Mock.class);
	}
	
	@Override
	public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
		return getMock(parameterContext.getParameter(), extensionContext);
	}
	
	private Object getMock(final Parameter parameter, final ExtensionContext extensionContext) {
		final Class<?> mockType = parameter.getType();
		final Store mocks = extensionContext.getStore(Namespace.create(MockitoExtension.class, mockType));
		final String mockName = getMockName(parameter);
		
		if (mockName != null) {
			return mocks.getOrComputeIfAbsent(mockName, key -> mock(mockType, mockName));
		} else {
			return mocks.getOrComputeIfAbsent(mockType.getCanonicalName(), key -> mock(mockType));
		}
	}
	
	private String getMockName(final Parameter parameter) {
		final String explicitMockName = parameter.getAnnotation(Mock.class).name().trim();
		if (!explicitMockName.isEmpty()) {
			return explicitMockName;
		} else if (parameter.isNamePresent()) {
			return parameter.getName();
		}
		return null;
	}
	
}