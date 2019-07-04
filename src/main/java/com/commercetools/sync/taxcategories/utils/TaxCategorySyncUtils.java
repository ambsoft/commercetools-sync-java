package com.commercetools.sync.taxcategories.utils;

import com.commercetools.sync.taxcategories.TaxCategorySyncOptions;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.taxcategories.TaxCategoryDraft;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.commercetools.sync.taxcategories.utils.TaxCategoryUpdateActionUtils.buildChangeNameAction;
import static com.commercetools.sync.taxcategories.utils.TaxCategoryUpdateActionUtils.buildRatesUpdateActions;
import static com.commercetools.sync.taxcategories.utils.TaxCategoryUpdateActionUtils.buildSetDescriptionAction;
import static com.commercetools.sync.commons.utils.OptionalUtils.filterEmptyOptionals;

public final class TaxCategorySyncUtils {

    private TaxCategorySyncUtils() {
    }

    /**
     * Compares all the fields (including the tax rates see
     * {@link TaxRatesUpdateActionUtils#buildTaxRatesUpdateActions(List, List)}) of a {@link TaxCategory} and a
     * {@link TaxCategoryDraft}. It returns a {@link List} of {@link UpdateAction}&lt;{@link TaxCategory}&gt; as a
     * result. If no update action is needed, for example in case where both the {@link TaxCategory} and the
     * {@link TaxCategoryDraft} have the same fields, an empty {@link List} is returned.
     *
     * @param oldTaxCategory the {@link TaxCategory} which should be updated.
     * @param newTaxCategory the {@link TaxCategoryDraft}  where we get the new data.
     * @param syncOptions    responsible for supplying the sync options to the sync utility method.
     *                       It is used for triggering the error callback within the utility, in case of
     *                       errors.
     * @return A list of tax category-specific update actions.
     */
    @Nonnull
    public static List<UpdateAction<TaxCategory>> buildActions(
            @Nonnull final TaxCategory oldTaxCategory,
            @Nonnull final TaxCategoryDraft newTaxCategory,
            @Nonnull final TaxCategorySyncOptions syncOptions) {

        final List<UpdateAction<TaxCategory>> updateActions = new ArrayList<>(
                filterEmptyOptionals(
                        buildChangeNameAction(oldTaxCategory, newTaxCategory),
                        buildSetDescriptionAction(oldTaxCategory, newTaxCategory)
                )
        );

        updateActions.addAll(buildRatesUpdateActions(oldTaxCategory, newTaxCategory, syncOptions));

        return updateActions;
    }

}