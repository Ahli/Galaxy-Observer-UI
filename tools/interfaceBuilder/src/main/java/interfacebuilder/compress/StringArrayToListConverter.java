// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.compress;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Converter that is automatically loaded by Hibernate without any references.
 */
@Converter
public class StringArrayToListConverter implements AttributeConverter<String[], List<String>> {
	
	@Override
	public List<String> convertToDatabaseColumn(final String[] attribute) {
		if (attribute == null || attribute.length <= 0) {
			return new ArrayList<>();
		}
		return Arrays.asList(attribute);
	}
	
	@Override
	public String[] convertToEntityAttribute(final List<String> dbData) {
		if (dbData == null || dbData.isEmpty()) {
			//noinspection ZeroLengthArrayAllocation
			return new String[0];
		}
		
		return dbData.toArray(new String[0]);
	}
	
}
