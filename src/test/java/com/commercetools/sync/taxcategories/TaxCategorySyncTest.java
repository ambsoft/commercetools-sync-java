package com.commercetools.sync.taxcategories;

import com.commercetools.sync.services.TaxCategoryService;
import com.commercetools.sync.taxcategories.helpers.TaxCategorySyncStatistics;
import io.sphere.sdk.client.ConcurrentModificationException;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.models.SphereException;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.taxcategories.TaxCategoryDraft;
import io.sphere.sdk.taxcategories.TaxCategoryDraftBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TaxCategorySyncTest {

    private TaxCategoryService taxCategoryService = mock(TaxCategoryService.class);

    @AfterEach
    void cleanup() {
        reset(taxCategoryService);
    }

    @Test
    void sync_WithInvalidDrafts_ShouldApplyErrorCallbackAndIncrementFailed() {
        List<String> errors = new ArrayList<>();

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .errorCallback((msg, error) -> errors.add(msg))
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft withoutKeyDraft = TaxCategoryDraftBuilder.of(null, emptyList(), null).build();

        TaxCategorySyncStatistics result = sync.sync(asList(null, withoutKeyDraft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(2),
            () -> assertThat(result.getFailed().get()).as("All prepared drafts should be faulty")
                .isEqualTo(2),
            () -> assertThat(errors).as("Error callback should be called for each draft").hasSize(2),
            () -> assertThat(errors).as("Error messages should contain proper reason")
                .contains("Failed to process null tax category draft.",
                    "Failed to process tax category draft without key.")
        );
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithErrorFetchingExistingKeys_ShouldApplyErrorCallbackAndIncrementFailed() {
        List<String> errors = new ArrayList<>();

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .errorCallback((msg, error) -> errors.add(msg))
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), null).key("someKey").build();

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any())).thenReturn(supplyAsync(() -> {
            throw new SphereException();
        }));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(errors).as("Error callback should be called").hasSize(1),
            () -> assertThat(errors).as("Error messages should contain proper reason")
                .contains("Failed to fetch existing taxCategories with keys: '[someKey]'.")
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithErrorCreating_ShouldIncrementFailedButNotApplyErrorCallback() {
        List<String> errors = new ArrayList<>();

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .errorCallback((msg, error) -> errors.add(msg))
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), null).key("someKey").build();

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any())).thenReturn(completedFuture(emptySet()));
        when(taxCategoryService.createTaxCategory(any())).thenReturn(completedFuture(empty()));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(result.getFailed().get()).as("Creation should fail")
                .isEqualTo(1),
            () -> assertThat(errors).as("Error callback should not be called").isEmpty()
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verify(taxCategoryService, times(1)).createTaxCategory(any());
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithNoError_ShouldApplyBeforeCreateCallbackAndIncrementCreated() {
        AtomicBoolean callbackApplied = new AtomicBoolean(false);

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .beforeCreateCallback((draft) -> {
                callbackApplied.set(true);
                return draft;
            })
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), null).key("someKey").build();
        TaxCategory taxCategory = mock(TaxCategory.class);

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any())).thenReturn(completedFuture(emptySet()));
        when(taxCategoryService.createTaxCategory(any())).thenReturn(completedFuture(Optional.of(taxCategory)));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(result.getCreated().get()).as("Creation should succeed")
                .isEqualTo(1),
            () -> assertThat(result.getFailed().get()).as("There should be no failures recorded")
                .isEqualTo(0),
            () -> assertThat(callbackApplied.get()).as("Before create callback should be called").isTrue()
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verify(taxCategoryService, times(1)).createTaxCategory(any());
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithErrorUpdating_ShouldApplyErrorCallbackAndIncrementFailed() {
        List<String> errors = new ArrayList<>();

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .errorCallback((msg, error) -> errors.add(msg))
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), "changed").key("someKey").build();

        TaxCategory taxCategory = mock(TaxCategory.class);
        when(taxCategory.getKey()).thenReturn("someKey");

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any()))
            .thenReturn(completedFuture(new HashSet<>(singletonList(taxCategory))));
        when(taxCategoryService.updateTaxCategory(any(), any())).thenReturn(supplyAsync(() -> {
            throw new SphereException();
        }));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(result.getUpdated().get()).as("Update should fail")
                .isEqualTo(0),
            () -> assertThat(result.getFailed().get()).as("There should be failure recorded")
                .isEqualTo(1),
            () -> assertThat(errors).as("Error callback should be called").hasSize(1)
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verify(taxCategoryService, times(1)).updateTaxCategory(any(), any());
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithErrorUpdatingAndTryingToRecoverWithFetchException_ShouldApplyErrorCallbackAndIncrementFailed() {
        List<String> errors = new ArrayList<>();

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .errorCallback((msg, error) -> errors.add(msg))
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), "changed").key("someKey").build();

        TaxCategory taxCategory = mock(TaxCategory.class);
        when(taxCategory.getKey()).thenReturn("someKey");

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any()))
            .thenReturn(completedFuture(new HashSet<>(singletonList(taxCategory))));
        when(taxCategoryService.updateTaxCategory(any(), any())).thenReturn(supplyAsync(() -> {
            throw new io.sphere.sdk.client.ConcurrentModificationException();
        }));
        when(taxCategoryService.fetchTaxCategory(any())).thenReturn(supplyAsync(() -> {
            throw new SphereException();
        }));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(result.getUpdated().get()).as("Update should fail")
                .isEqualTo(0),
            () -> assertThat(result.getFailed().get()).as("There should be failure recorded")
                .isEqualTo(1),
            () -> assertThat(errors).as("Error callback should be called").hasSize(1),
            () -> assertThat(errors).as("Error should contain reason of failure")
                .hasOnlyOneElementSatisfying(msg -> assertThat(msg)
                    .contains("Failed to fetch from CTP while retrying after concurrency modification."))
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verify(taxCategoryService, times(1)).updateTaxCategory(any(), any());
        verify(taxCategoryService, times(1)).fetchTaxCategory(any());
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithErrorUpdatingAndTryingToRecoverWithEmptyResponse_ShouldApplyErrorCallbackAndIncrementFailed() {
        List<String> errors = new ArrayList<>();

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .errorCallback((msg, error) -> errors.add(msg))
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), "changed").key("someKey").build();

        TaxCategory taxCategory = mock(TaxCategory.class);
        when(taxCategory.getKey()).thenReturn("someKey");

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any()))
            .thenReturn(completedFuture(new HashSet<>(singletonList(taxCategory))));
        when(taxCategoryService.updateTaxCategory(any(), any())).thenReturn(supplyAsync(() -> {
            throw new ConcurrentModificationException();
        }));
        when(taxCategoryService.fetchTaxCategory(any())).thenReturn(completedFuture(Optional.empty()));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(result.getUpdated().get()).as("Update should fail")
                .isEqualTo(0),
            () -> assertThat(result.getFailed().get()).as("There should be failure recorded")
                .isEqualTo(1),
            () -> assertThat(errors).as("Error callback should be called").hasSize(1),
            () -> assertThat(errors).as("Error should contain reason of failure")
                .hasOnlyOneElementSatisfying(msg -> assertThat(msg)
                    .contains("Not found when attempting to fetch while retrying after "
                        + "concurrency modification."))
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verify(taxCategoryService, times(1)).updateTaxCategory(any(), any());
        verify(taxCategoryService, times(1)).fetchTaxCategory(any());
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithNoError_ShouldApplyBeforeUpdateCallbackAndIncrementUpdated() {
        AtomicBoolean callbackApplied = new AtomicBoolean(false);

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .beforeUpdateCallback((actions, draft, old) -> {
                callbackApplied.set(true);
                return actions;
            })
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), "changed").key("someKey").build();

        TaxCategory taxCategory = mock(TaxCategory.class);
        when(taxCategory.getId()).thenReturn("id");
        when(taxCategory.getKey()).thenReturn("someKey");

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any()))
            .thenReturn(completedFuture(new HashSet<>(singletonList(taxCategory))));
        when(taxCategoryService.updateTaxCategory(any(), any())).thenReturn(completedFuture(taxCategory));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(result.getUpdated().get()).as("Update should succeed")
                .isEqualTo(1),
            () -> assertThat(result.getFailed().get()).as("There should be no failures recorded")
                .isEqualTo(0),
            () -> assertThat(callbackApplied.get()).as("Before update callback should be called").isTrue()
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verify(taxCategoryService, times(1)).updateTaxCategory(any(), any());
        verifyNoMoreInteractions(taxCategoryService);
    }

    @Test
    void sync_WithFilteredActions_ShouldApplyBeforeUpdateCallbackAndNotIncrementUpdated() {
        AtomicBoolean callbackApplied = new AtomicBoolean(false);

        TaxCategorySyncOptions options = TaxCategorySyncOptionsBuilder.of(mock(SphereClient.class))
            .beforeUpdateCallback((actions, draft, old) -> {
                callbackApplied.set(true);
                return emptyList();
            })
            .build();
        TaxCategorySync sync = new TaxCategorySync(options, taxCategoryService);

        TaxCategoryDraft draft = TaxCategoryDraftBuilder.of("someName", emptyList(), "changed").key("someKey").build();

        TaxCategory taxCategory = mock(TaxCategory.class);
        when(taxCategory.getId()).thenReturn("id");
        when(taxCategory.getKey()).thenReturn("someKey");

        when(taxCategoryService.fetchMatchingTaxCategoriesByKeys(any()))
            .thenReturn(completedFuture(new HashSet<>(singletonList(taxCategory))));

        TaxCategorySyncStatistics result = sync.sync(singletonList(draft)).toCompletableFuture().join();

        assertAll(
            () -> assertThat(result.getProcessed().get()).as("All prepared drafts should be processed")
                .isEqualTo(1),
            () -> assertThat(result.getUpdated().get()).as("Update should succeed")
                .isEqualTo(0),
            () -> assertThat(result.getFailed().get()).as("There should be no failures recorded")
                .isEqualTo(0),
            () -> assertThat(callbackApplied.get()).as("Before update callback should be called").isTrue()
        );
        verify(taxCategoryService, times(1)).fetchMatchingTaxCategoriesByKeys(any());
        verifyNoMoreInteractions(taxCategoryService);
    }

}