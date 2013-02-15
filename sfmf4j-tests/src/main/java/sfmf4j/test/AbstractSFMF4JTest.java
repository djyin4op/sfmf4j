/*
 * Copyright 2012-2013 Steven Swor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sfmf4j.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sfmf4j.api.DirectoryListener;
import sfmf4j.api.FileMonitorService;

/**
 * Parent class for testing implementations.
 *
 * @author sswor
 */
public abstract class AbstractSFMF4JTest {

    /**
     * The timeout duration when verifying events have been fired.
     *
     * @return the timeout duration when verifying events have been fired
     */
    protected long eventTimeoutDuration() {
        if (System.getProperty("os.name").contains("Mac OS X")) {
            return 120;
        } else {
            return 3;
        }
    }

    /**
     * The timeout time unit when verifying events have been fired.
     *
     * @return the timout time unit when verifying events have been fired
     */
    protected TimeUnit eventTimeoutTimeUnit() {
        return TimeUnit.SECONDS;
    }
    /**
     * Temporary folder used for creating, modifying, and deleting files and
     * folders.
     */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Performs the tests for the implementation by creating, modifying, and
     * deleting files.
     *
     * @param fileMonitor the implementation to test
     * @throws Throwable if the test fails
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    protected void doTestFileMonitoring(final FileMonitorService fileMonitor) throws Throwable {
        final Logger logger = LoggerFactory.getLogger(getClass());
        File newFile = null;
        final BlockingQueue<File> createdFiles = new LinkedBlockingQueue<File>();
        final BlockingQueue<File> modifiedFiles = new LinkedBlockingQueue<File>();
        final BlockingQueue<File> deletedFiles = new LinkedBlockingQueue<File>();
        final DirectoryListener listener = new DirectoryListener() {
            @Override
            public void fileCreated(File created) {
                createdFiles.add(created);
            }

            @Override
            public void fileChanged(File changed) {
                modifiedFiles.add(changed);
            }

            @Override
            public void fileDeleted(File deleted) {
                deletedFiles.add(deleted);
            }
        };
        File folder = null;
        try {
            folder = tempFolder.getRoot();
            fileMonitor.registerDirectoryListener(folder, listener);
            assertTrue(fileMonitor.isMonitoringDirectory(folder));

            /*
             * Verify that we don't unregister somehow if we accidentally
             * registered twice.
             */
            fileMonitor.registerDirectoryListener(folder, listener);
            assertTrue(fileMonitor.isMonitoringDirectory(folder));

            /*
             * Create a new file.
             */
            newFile = tempFolder.newFile();
            logger.debug("Test file: {}", newFile.getAbsolutePath());
            logger.info("Testing for created event.");
            File created = createdFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit());
            assertNotNull(created);
            assertEquals(newFile.getAbsolutePath(), created.getAbsolutePath());

            /*
             * Write to the file.
             */
            logger.info("Testing for modification event.");
            appendBytesToFile(newFile);
            File modified = modifiedFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit());
            assertNotNull(modified);
            assertEquals(newFile.getAbsolutePath(), modified.getAbsolutePath());

            /*
             * Test deletion.
             */
            newFile.delete();
            File deleted = deletedFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit());
            assertNotNull(deleted);
            assertEquals(newFile.getAbsolutePath(), deleted.getAbsolutePath());

            /*
             * Test unregistration.
             */
            fileMonitor.unregisterDirectoryListener(folder, listener);
            assertFalse(fileMonitor.isMonitoringDirectory(folder));
            File createdAfterUnregister = tempFolder.newFile();
            assertNull(createdFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit()));
            appendBytesToFile(createdAfterUnregister);
            assertNull(modifiedFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit()));
            createdAfterUnregister.delete();
            assertNull(deletedFiles.poll(eventTimeoutDuration(), eventTimeoutTimeUnit()));
        } finally {
            if (folder != null) {
                try {
                    fileMonitor.unregisterDirectoryListener(folder, listener);
                } catch (Exception ex) {
                    //trap
                }
            }
            if (newFile != null && newFile.exists()) {
                try {
                    newFile.delete();
                } catch (Exception ex) {
                    //trap
                }
            }
        }
    }

    /**
     * Appends bytes to a file.
     * @param f the file
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException if the file cannot be written
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    private void appendBytesToFile(final File f) throws FileNotFoundException, IOException {
        FileOutputStream fileOut = null;
        byte[] bytes = new byte[4096];
        try {
            fileOut = new FileOutputStream(f, true);
            fileOut.write(bytes);
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (Exception ex) {
                    //trap.  We'll have bigger fish to fry if this happens
                }
            }
        }
    }
}
