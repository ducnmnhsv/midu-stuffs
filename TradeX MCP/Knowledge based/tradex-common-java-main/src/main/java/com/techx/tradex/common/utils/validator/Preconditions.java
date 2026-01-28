package com.techx.tradex.common.utils.validator;

import com.techx.tradex.common.exceptions.GeneralException;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class Preconditions {
	private Preconditions() {}

	public static void checkArgument(boolean expression, @Nullable Object errorMessage) {
		if (!expression) {
			throw new GeneralException(String.valueOf(errorMessage));
		}
	}
}
