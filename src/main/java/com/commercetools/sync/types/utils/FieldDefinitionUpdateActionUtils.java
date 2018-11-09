package com.commercetools.sync.types.utils;

import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.models.LocalizedEnumValue;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.types.EnumFieldType;
import io.sphere.sdk.types.FieldDefinition;
import io.sphere.sdk.types.LocalizedEnumFieldType;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.commands.updateactions.ChangeFieldDefinitionLabel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.commercetools.sync.commons.utils.CommonTypeUpdateActionUtils.buildUpdateAction;
import static com.commercetools.sync.commons.utils.OptionalUtils.filterEmptyOptionals;
import static com.commercetools.sync.types.utils.LocalizedEnumValueUpdateActionUtils.buildLocalizedEnumValuesUpdateActions;
import static com.commercetools.sync.types.utils.PlainEnumValueUpdateActionUtils.buildEnumValuesUpdateActions;

/**
 * This class is only meant for the internal use of the commercetools-sync-java library.
 */
final class FieldDefinitionUpdateActionUtils {

    /**
     * Compares all the fields of old {@link FieldDefinition} with new {@link FieldDefinition} and returns
     * a list of {@link UpdateAction}&lt;{@link Type}&gt; as a result. If both the {@link FieldDefinition}
     * and the {@link FieldDefinition} have identical fields, then no update action is needed and hence an
     * empty {@link List} is returned.
     *
     * @param oldFieldDefinition the old field definition which should be updated.
     * @param newFieldDefinition the new field definition where we get the new fields.
     * @return A list with the update actions or an empty list if the field definition fields are identical.
     */
    @Nonnull
    static List<UpdateAction<Type>> buildActions(
        @Nonnull final FieldDefinition oldFieldDefinition,
        @Nonnull final FieldDefinition newFieldDefinition) {

        final List<UpdateAction<Type>> updateActions =
            filterEmptyOptionals(buildChangeLabelUpdateAction(oldFieldDefinition, newFieldDefinition));

        updateActions.addAll(buildEnumUpdateActions(oldFieldDefinition, newFieldDefinition));

        return updateActions;
    }

    /**
     * Compares all the {@link EnumValue} and {@link LocalizedEnumValue} values of {@link FieldDefinition}s and returns
     * a list of {@link UpdateAction}&lt;{@link Type}&gt; as a result. If both {@link FieldDefinition}s have identical
     * enum values, then no update action is needed and hence an empty {@link List} is returned.
     *
     * @param oldFieldDefinition the old field definition which should be updated.
     * @param newFieldDefinition the new field definition where we get the new fields.
     * @return A list with the update actions or an empty list if the field definition enums are identical.
     */
    @Nonnull
    static List<UpdateAction<Type>> buildEnumUpdateActions(
        @Nonnull final FieldDefinition oldFieldDefinition,
        @Nonnull final FieldDefinition newFieldDefinition) {

        final List<UpdateAction<Type>> updateActions = new ArrayList<>();
        if (isPlainEnumField(oldFieldDefinition)) {
            updateActions.addAll(buildEnumValuesUpdateActions(
                oldFieldDefinition.getName(),
                ((EnumFieldType) oldFieldDefinition.getType()).getValues(),
                ((EnumFieldType) newFieldDefinition.getType()).getValues()
            ));
        } else if (isLocalizedEnumField(oldFieldDefinition)) {
            updateActions.addAll(buildLocalizedEnumValuesUpdateActions(
                oldFieldDefinition.getName(),
                ((LocalizedEnumFieldType) oldFieldDefinition.getType()).getValues(),
                ((LocalizedEnumFieldType) newFieldDefinition.getType()).getValues()
            ));
        }
        return updateActions;
    }

    /**
     * Indicates if the field is a plain enum value or not.
     *
     * @param fieldDefinition the field definition.
     * @return true if the field definition is a plain enum value, false otherwise.
     */
    private static boolean isPlainEnumField(@Nonnull final FieldDefinition fieldDefinition) {
        return fieldDefinition.getType().getClass() == EnumFieldType.class;
    }

    /**
     * Indicates if the field definition is a localized enum value or not.
     *
     * @param fieldDefinition the field definition.
     * @return true if the field definition is a localized enum value, false otherwise.
     */
    private static boolean isLocalizedEnumField(@Nonnull final FieldDefinition fieldDefinition) {
        return fieldDefinition.getType().getClass() == LocalizedEnumFieldType.class;
    }

    /**
     * Compares the {@link LocalizedString} labels of old {@link FieldDefinition} with new
     * {@link FieldDefinition} and returns an {@link UpdateAction}&lt;{@link Type}&gt; as a result in
     * an {@link Optional}. If both the {@link FieldDefinition} and the {@link FieldDefinition} have the
     * same label, then no update action is needed and hence an empty {@link Optional} is returned.
     *
     * @param oldFieldDefinition the old field definition which should be updated.
     * @param newFieldDefinition the new field definition draft where we get the new label.
     * @return A filled optional with the update action or an empty optional if the labels are identical.
     */
    @Nonnull
    static Optional<UpdateAction<Type>> buildChangeLabelUpdateAction(
        @Nonnull final FieldDefinition oldFieldDefinition,
        @Nonnull final FieldDefinition newFieldDefinition) {

        return buildUpdateAction(oldFieldDefinition.getLabel(), newFieldDefinition.getLabel(),
            () -> ChangeFieldDefinitionLabel.of(oldFieldDefinition.getName(), newFieldDefinition.getLabel())
        );
    }



    private FieldDefinitionUpdateActionUtils() {
    }
}