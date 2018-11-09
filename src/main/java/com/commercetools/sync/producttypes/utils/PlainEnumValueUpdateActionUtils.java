package com.commercetools.sync.producttypes.utils;

import com.commercetools.sync.commons.utils.enums.EnumValuesUpdateActionUtils;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.producttypes.ProductType;
import io.sphere.sdk.producttypes.commands.updateactions.AddEnumValue;
import io.sphere.sdk.producttypes.commands.updateactions.ChangeEnumValueOrder;
import io.sphere.sdk.producttypes.commands.updateactions.ChangePlainEnumValueLabel;
import io.sphere.sdk.producttypes.commands.updateactions.RemoveEnumValues;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.commercetools.sync.commons.utils.OptionalUtils.filterEmptyOptionals;
import static com.commercetools.sync.commons.utils.enums.PlainEnumValueUpdateActionUtils.buildChangeLabelAction;

public final class PlainEnumValueUpdateActionUtils {

    /**
     * Compares a list of old {@link EnumValue}s with a list of new {@link EnumValue}s for a given
     * attribute definition.
     * The method serves as a generic implementation for plain enum values syncing. The method takes in functions
     * for building the required update actions (AddEnumValue, RemoveEnumValue, ChangeEnumValueOrder
     * and 1-1 update actions on plain enum values (e.g. changeLabel) for the required resource.
     *
     * <p>If the list of new {@link EnumValue}s is {@code null}, then remove actions are built for
     * every existing plain enum value in the {@code oldEnumValues} list.
     *
     * @param attributeDefinitionName the attribute name whose plain enum values are going to be synced.
     * @param oldEnumValues           the old list of plain enum values.
     * @param newEnumValues           the new list of plain enum values.
     * @return a list of plain enum values update actions if the list of plain enum values is not identical.
     *         Otherwise, if the plain enum values are identical, an empty list is returned.
     */
    @Nonnull
    public static List<UpdateAction<ProductType>> buildEnumValuesUpdateActions(
        @Nonnull final String attributeDefinitionName,
        @Nonnull final List<EnumValue> oldEnumValues,
        @Nullable final List<EnumValue> newEnumValues) {

        return EnumValuesUpdateActionUtils.buildActions(attributeDefinitionName,
                oldEnumValues,
                newEnumValues,
                RemoveEnumValues::of,
                PlainEnumValueUpdateActionUtils::buildEnumValueUpdateActions,
                AddEnumValue::of,
                ChangeEnumValueOrder::of,
                null);
    }

    /**
     * Compares all the fields of an old {@link EnumValue} and a new {@link EnumValue} and returns a list of
     * {@link UpdateAction}&lt;{@link ProductType}&gt; as a result. If both {@link EnumValue} have identical fields,
     * then no update action is needed and hence an empty {@link List} is returned.
     *
     * @param attributeDefinitionName the attribute definition name whose plain enum values belong to.
     * @param oldEnumValue            the enum value which should be updated.
     * @param newEnumValue            the enum value where we get the new fields.
     * @return A list with the update actions or an empty list if the enum values are
     *         identical.
     */
    @Nonnull
    public static List<UpdateAction<ProductType>> buildEnumValueUpdateActions(
            @Nonnull final String attributeDefinitionName,
            @Nonnull final EnumValue oldEnumValue,
            @Nonnull final EnumValue newEnumValue) {

        return filterEmptyOptionals(
                buildChangeLabelAction(attributeDefinitionName,
                        oldEnumValue,
                        newEnumValue,
                        ChangePlainEnumValueLabel::of
                )
        );
    }


    private PlainEnumValueUpdateActionUtils() {
    }
}