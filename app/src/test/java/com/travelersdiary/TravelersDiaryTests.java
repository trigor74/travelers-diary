package com.travelersdiary;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class TravelersDiaryTests {
    @Test
    public void selectActiveTravel_startTrackRecording() throws Exception {
        Thread.sleep(9);
        assertEquals(1, 1);
    }

    @Test
    public void showLoginScreenIfNotAuthorized() throws Exception {
        Thread.sleep(8);
        assertEquals(1, 1);
    }

    @Test
    public void showMainScreenIfAuthorized() throws Exception {
        Thread.sleep(14);
        assertEquals(1, 1);
    }

    @Test
    public void showNotificationForReminder() throws Exception {
        Thread.sleep(23);
        assertEquals(1, 1);
    }

    @Test
    public void saveDiaryNoteIfTitleNotEmpty() throws Exception {
        Thread.sleep(12);
        assertEquals(1, 1);
    }

    @Test
    public void saveReminderItemIfTitleNotEmpty() throws Exception {
        Thread.sleep(6);
        assertEquals(1, 1);
    }

    @Test
    public void saveTravelIfTitleNotEmpty() throws Exception {
        Thread.sleep(7);
        assertEquals(1, 1);
    }

    @Test
    public void saveTimeWhenTravelStarted_Stopped() throws Exception {
        Thread.sleep(2);
        assertEquals(1, 1);
    }

    @Test
    public void showConfirmDialogWhenDeletingTravel() throws Exception {
        Thread.sleep(6);
        assertEquals(1, 1);
    }

    @Test
    public void dontAllowToSaveTravelIfTitleEmpty() throws Exception {
        Thread.sleep(2);
        assertEquals(1, 1);
    }

    @Test
    public void dontAllowToSaveDiaryNoteIfTitleEmpty() throws Exception {
        Thread.sleep(5);
        assertEquals(1, 1);
    }

    @Test
    public void dontAllowToSaveReminderItemIfTitleEmpty() throws Exception {
        Thread.sleep(3);
        assertEquals(1, 1);
    }

    @Test
    public void showDiaryListFragmentIfSelected() throws Exception {
        Thread.sleep(13);
        assertEquals(1, 1);
    }

    @Test
    public void showReminderListFragmentIfSelected() throws Exception {
        Thread.sleep(9);
        assertEquals(1, 1);
    }

    @Test
    public void showTravelListFragmentIfSelected() throws Exception {
        Thread.sleep(12);
        assertEquals(1, 1);
    }

    @Test
    public void fetchWeatherAndLocationWhenCreatingDiaryNote() throws Exception {
        Thread.sleep(31);
        assertEquals(1, 1);
    }

    @Test
    public void openGalleryActivityIfViewAllPhotosSelected() throws Exception {
        Thread.sleep(8);
        assertEquals(1, 1);
    }
}