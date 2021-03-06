/* 
 * Copyright (c) 2014, 2015 Data4All
 * 
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     <p>http://www.apache.org/licenses/LICENSE-2.0
 * 
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.data4all.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.location.Location;

/**
 * Test cases for the Optimizer class
 * 
 * @author sbollen
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class OptimizerTest {

    // different locations for comparing
    Location newLocation;
    Location oldLocation;
    Location location1;
    Location location2;
    Location location3;
    Location accurateLocation;
    Location nullLocation;

    // significant time difference and actual time for comparing location time
    // stamps
    double timeDiff;
    long time;

    @Before
    public void setUp() throws Exception {

        newLocation = new Location("provider");
        oldLocation = new Location("provider");
        location1 = new Location("provider");
        location2 = new Location("provider");
        location3 = new Location("provider");
        accurateLocation = new Location("provider");
        nullLocation = null;

        // get the time difference for comparing locations here because this
        // value can change in the further progress
        timeDiff = Optimizer.TIME_DIFFERENCE;
        // set a time
        time = System.currentTimeMillis();

        // the new location gets a significant bigger time than the old one
        newLocation.setTime((long) (2 * timeDiff));
        oldLocation.setTime(0);

        // set a better accuracy for accurateLocation than for location1
        accurateLocation.setTime(time);
        accurateLocation.setAccuracy(1);
        location1.setTime(time);
        location1.setAccuracy(2);

        // location3 is newer, not significantly less accurate and has the same
        // provider as location4
        location2.setTime(time);
        location3.setTime(time + 1);
        location2.setAccuracy(4);
        location3.setAccuracy(3);
        location2.setProvider("provider");
        location3.setProvider("provider");

    }

    // tests for calculateBestLoc

    /**
     * Checks four different possibilities. If there is no location. If only one
     * location is in the buffer it is the best. If a second better location is
     * put in the buffer. If a third worse location is put in the buffer.
     */
    @Test
    public void testCalculateBestLoc() {
        assertNull(Optimizer.calculateBestLoc());
        // put one location in the buffer and this has to be the best
        // location
        Optimizer.putLoc(oldLocation);
        assertEquals(oldLocation, Optimizer.calculateBestLoc());
        // put another location in the buffer which is better than the first
        // one
        Optimizer.putLoc(newLocation);
        assertEquals(newLocation, Optimizer.calculateBestLoc());
        // put another location in the buffer which is not better than the
        // one before
        Optimizer.putLoc(oldLocation);
        assertEquals(newLocation, Optimizer.calculateBestLoc());
    }

    // tests for isBetterLocation

    /**
     * If the second location is a null location.
     */
    @Test
    public void testIsBetterLocation_SecondIsNull() {
        assertTrue(Optimizer.isBetterLocation(location1, nullLocation));
    }

    /**
     * If the first location is a significantly newer location.
     */
    @Test
    public void testIsBetterLocation_FirstIsSigNewer() {
        assertTrue(Optimizer.isBetterLocation(newLocation, oldLocation));
    }

    /**
     * If the first location is a significantly older location.
     */
    @Test
    public void testIsBetterLocation_FirstIsSigOlder() {
        assertFalse(Optimizer.isBetterLocation(oldLocation, newLocation));
    }

    /**
     * If the first location is more accurate and there is no significant time
     * difference.
     */
    @Test
    public void testIsBetterLocation_FirstIsMoreAccurate() {
        assertTrue(Optimizer.isBetterLocation(accurateLocation, location1));
    }

    /**
     * If the first location is newer and not less accurate.
     */
    @Test
    public void testIsBetterLocation_FirstIsNewer() {
        // set the time of the second location down so that it is older
        location1.setTime(time - 1);
        // set the accuracy of the second location to the same value as the
        // first location so that the first one is not less accurate
        location1.setAccuracy(1);
        assertTrue(Optimizer.isBetterLocation(accurateLocation, location1));
    }

    /**
     * If the first location is newer, not significantly less accurate and from
     * the same provider.
     */
    @Test
    public void testIsBetterLocation_FirstIsNewer2() {
        assertTrue(Optimizer.isBetterLocation(location3, location2));
    }

    /**
     * If the first location is older but not significantly older and not less
     * or more accurate.
     */
    @Test
    public void testIsBetterLocation_FirstIsNotSigOlder() {
        location3.setTime(time - 1);
        location3.setAccuracy(4);
        assertFalse(Optimizer.isBetterLocation(location3, location2));
    }
}
