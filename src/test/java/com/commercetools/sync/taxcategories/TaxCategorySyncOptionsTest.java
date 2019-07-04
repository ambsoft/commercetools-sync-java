package com.commercetools.sync.taxcategories;

import com.commercetools.sync.commons.utils.TriFunction;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.taxcategories.TaxCategoryDraft;
import io.sphere.sdk.taxcategories.TaxCategoryDraftBuilder;
import io.sphere.sdk.taxcategories.commands.updateactions.SetKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaxCategorySyncOptionsTest {

    private static SphereClient CTP_CLIENT = mock(SphereClient.class);

    @Test
    void applyBeforeUpdateCallback_WithNullCallback_ShouldReturnIdenticalList() {
        TaxCategorySyncOptions stateSyncOptions = TaxCategorySyncOptionsBuilder.of(CTP_CLIENT)
            .build();
        assertThat(stateSyncOptions.getBeforeUpdateCallback()).isNull();

        List<UpdateAction<TaxCategory>> updateActions = singletonList(SetKey.of("key"));
        List<UpdateAction<TaxCategory>> filteredList = stateSyncOptions
            .applyBeforeUpdateCallBack(updateActions, mock(TaxCategoryDraft.class), mock(TaxCategory.class));

        assertThat(filteredList).as(" returned 'updateActions' should not be changed")
            .isSameAs(updateActions);
    }

    @Test
    void applyBeforeUpdateCallback_WithNullReturnCallback_ShouldReturnEmptyList() {
        TriFunction<List<UpdateAction<TaxCategory>>, TaxCategoryDraft, TaxCategory, List<UpdateAction<TaxCategory>>>
            beforeUpdateCallback = (updateActions, newCategory, oldCategory) -> null;
        TaxCategorySyncOptions stateSyncOptions = TaxCategorySyncOptionsBuilder.of(CTP_CLIENT)
            .beforeUpdateCallback(beforeUpdateCallback)
            .build();
        assertThat(stateSyncOptions.getBeforeUpdateCallback()).isNotNull();

        List<UpdateAction<TaxCategory>> updateActions = singletonList(SetKey.of("key"));
        List<UpdateAction<TaxCategory>> filteredList = stateSyncOptions
            .applyBeforeUpdateCallBack(updateActions, mock(TaxCategoryDraft.class), mock(TaxCategory.class));

        assertAll(
            () -> assertThat(filteredList)
                .as("returned 'updateActions' should not be equal to prepared ones")
                .isNotEqualTo(updateActions),
            () -> assertThat(filteredList).as("returned 'updateActions' should be empty").isEmpty()
        );
    }

    private interface MockTriFunction extends
        TriFunction<List<UpdateAction<TaxCategory>>, TaxCategoryDraft, TaxCategory, List<UpdateAction<TaxCategory>>> {
    }

    @Test
    void applyBeforeUpdateCallback_WithEmptyUpdateActions_ShouldNotApplyBeforeUpdateCallback() {
        TaxCategorySyncOptionsTest.MockTriFunction beforeUpdateCallback =
            mock(TaxCategorySyncOptionsTest.MockTriFunction.class);

        TaxCategorySyncOptions stateSyncOptions = TaxCategorySyncOptionsBuilder.of(CTP_CLIENT)
            .beforeUpdateCallback(beforeUpdateCallback)
            .build();

        assertThat(stateSyncOptions.getBeforeUpdateCallback()).isNotNull();

        List<UpdateAction<TaxCategory>> updateActions = emptyList();
        List<UpdateAction<TaxCategory>> filteredList = stateSyncOptions
            .applyBeforeUpdateCallBack(updateActions, mock(TaxCategoryDraft.class), mock(TaxCategory.class));

        assertThat(filteredList).as("returned 'updateActions' should be empty").isEmpty();
        verify(beforeUpdateCallback, never()).apply(any(), any(), any());
    }

    @Test
    void applyBeforeUpdateCallback_WithCallback_ShouldReturnFilteredList() {
        TriFunction<List<UpdateAction<TaxCategory>>, TaxCategoryDraft, TaxCategory, List<UpdateAction<TaxCategory>>>
            beforeUpdateCallback = (updateActions, newCategory, oldCategory) -> emptyList();
        TaxCategorySyncOptions stateSyncOptions = TaxCategorySyncOptionsBuilder.of(CTP_CLIENT)
            .beforeUpdateCallback(beforeUpdateCallback)
            .build();
        assertThat(stateSyncOptions.getBeforeUpdateCallback()).isNotNull();

        List<UpdateAction<TaxCategory>> updateActions = singletonList(SetKey.of("key"));
        List<UpdateAction<TaxCategory>> filteredList = stateSyncOptions
            .applyBeforeUpdateCallBack(updateActions, mock(TaxCategoryDraft.class), mock(TaxCategory.class));

        assertAll(
            () -> assertThat(filteredList)
                .as("returned 'updateActions' should not be equal to prepared ones")
                .isNotEqualTo(updateActions),
            () -> assertThat(filteredList).as("returned 'updateActions' should be empty").isEmpty()
        );
    }

    @Test
    void applyBeforeCreateCallback_WithCallback_ShouldReturnFilteredDraft() {
        Function<TaxCategoryDraft, TaxCategoryDraft> draftFunction =
            stateDraft -> TaxCategoryDraftBuilder.of(stateDraft).key(stateDraft.getKey() + "_filteredKey").build();

        TaxCategorySyncOptions stateSyncOptions = TaxCategorySyncOptionsBuilder.of(CTP_CLIENT)
            .beforeCreateCallback(draftFunction)
            .build();
        assertThat(stateSyncOptions.getBeforeCreateCallback()).isNotNull();

        TaxCategoryDraft resourceDraft = mock(TaxCategoryDraft.class);
        when(resourceDraft.getKey()).thenReturn("myKey");

        Optional<TaxCategoryDraft> filteredDraft = stateSyncOptions.applyBeforeCreateCallBack(resourceDraft);

        assertAll(
            () -> assertThat(filteredDraft).as("should return draft").isNotEmpty(),
            () -> assertThat(filteredDraft.get().getKey())
                .as("returned 'draft' should have different key")
                .isEqualTo("myKey_filteredKey")
        );
    }

    @Test
    void applyBeforeCreateCallback_WithNullCallback_ShouldReturnIdenticalDraftInOptional() {
        TaxCategorySyncOptions stateSyncOptions = TaxCategorySyncOptionsBuilder.of(CTP_CLIENT).build();
        assertThat(stateSyncOptions.getBeforeCreateCallback()).isNull();

        TaxCategoryDraft resourceDraft = mock(TaxCategoryDraft.class);
        Optional<TaxCategoryDraft> filteredDraft = stateSyncOptions.applyBeforeCreateCallBack(resourceDraft);

        assertThat(filteredDraft).as("returned 'draft' should not be changed").containsSame(resourceDraft);
    }

    @Test
    void applyBeforeCreateCallback_WithCallbackReturningNull_ShouldReturnEmptyOptional() {
        Function<TaxCategoryDraft, TaxCategoryDraft> draftFunction = stateDraft -> null;
        TaxCategorySyncOptions stateSyncOptions = TaxCategorySyncOptionsBuilder.of(CTP_CLIENT)
            .beforeCreateCallback(draftFunction)
            .build();
        assertThat(stateSyncOptions.getBeforeCreateCallback()).isNotNull();

        TaxCategoryDraft resourceDraft = mock(TaxCategoryDraft.class);
        Optional<TaxCategoryDraft> filteredDraft = stateSyncOptions.applyBeforeCreateCallBack(resourceDraft);

        assertThat(filteredDraft).as("should return no draft").isEmpty();
    }

}