package com.commercetools.sync.types.utils;

import com.commercetools.sync.commons.utils.EnumValuesUpdateActionUtils;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.commands.updateactions.AddEnumValue;
import io.sphere.sdk.types.commands.updateactions.ChangeEnumValueOrder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class PlainEnumValueUpdateActionUtils {

    /**
     * Compares a list of old {@link EnumValue}s with a list of new {@link EnumValue}s for a given
     * field definition.
     * The method serves as an implementation for plain enum values syncing. The method takes in functions
     * for building the required update actions (AddEnumValue, ChangeEnumValueOrder
     * and 1-1 update actions on plain enum values (e.g. changeLabel) for the required resource.
     *
     * @param fieldDefinitionName     the field name whose plain enum values are going to be synced.
     * @param oldEnumValues           the old list of plain enum values.
     * @param newEnumValues           the new list of plain enum values.
     * @return a list of plain enum values update actions if the list of plain enum values is not identical.
     *         Otherwise, if the plain enum values are identical, an empty list is returned.
     */
    @Nonnull
    public static List<UpdateAction<Type>> buildEnumValuesUpdateActions(
        @Nonnull final String fieldDefinitionName,
        @Nonnull final List<EnumValue> oldEnumValues,
        @Nullable final List<EnumValue> newEnumValues) {

        /*
          TODO: If the list of newEnumValues is null, then remove actions are built
                for every existing plain enum value in the oldEnumValues list.
         */

        return EnumValuesUpdateActionUtils.buildActions(fieldDefinitionName,
                oldEnumValues,
                newEnumValues,
                null,
                null,
                AddEnumValue::of,
                null,
                ChangeEnumValueOrder::of);
    }

    private PlainEnumValueUpdateActionUtils() {
    }
}
