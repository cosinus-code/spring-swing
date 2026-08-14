/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.test.robot;

import org.assertj.swing.core.ComponentFinder;
import org.assertj.swing.core.ComponentPrinter;
import org.assertj.swing.core.MouseButton;
import org.assertj.swing.core.Robot;
import org.assertj.swing.core.Settings;
import org.assertj.swing.fixture.JMenuItemFixture;
import org.assertj.swing.hierarchy.ComponentHierarchy;
import org.cosinus.swing.menu.MenuItem;

import javax.swing.*;
import java.awt.*;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

public class SwingRobot implements Robot {

    private final Robot robot;

    public SwingRobot(Robot robot) {
        this.robot = robot;
    }

    public void clickMenuItem(String menuKey) {
        ofNullable(applicationFrame.getMenu())
            .map(menuBar -> menuBar.getMenuItem(menuKey))
            .ifPresentOrElse(
                this::clickMenuItem,
                () -> {
                    throw new IllegalStateException("Menu item is not available in application frame: " + menuKey);
                }
            );
    }

    public void clickMenuItem(MenuItem menuItem) {
        new JMenuItemFixture(robot, menuItem).click();
    }

    @Override
    public ComponentHierarchy hierarchy() {
        return robot.hierarchy();
    }

    @Override
    public ComponentFinder finder() {
        return robot.finder();
    }

    @Override
    public ComponentPrinter printer() {
        return robot.printer();
    }

    @Override
    public void showWindow(Window w) {
        robot.showWindow(w);
    }

    @Override
    public void showWindow(Window w, Dimension size) {
        robot.showWindow(w, size);
    }

    @Override
    public void showWindow(Window w, Dimension size, boolean pack) {
        robot.showWindow(w, size, pack);
    }

    @Override
    public void close(Window w) {
        robot.close(w);
    }

    @Override
    public void focus(Component c) {
        robot.focus(c);
    }

    @Override
    public void focusAndWaitForFocusGain(Component c) {
        robot.focusAndWaitForFocusGain(c);
    }

    @Override
    public void cleanUp() {
        robot.cleanUp();
    }

    @Override
    public void cleanUpWithoutDisposingWindows() {
        robot.cleanUpWithoutDisposingWindows();
    }

    @Override
    public void click(Component c) {
        robot.click(c);
    }

    @Override
    public void rightClick(Component c) {
        robot.rightClick(c);
    }

    @Override
    public void click(Component c, MouseButton button) {
        robot.click(c, button);
    }

    @Override
    public void doubleClick(Component c) {
        robot.doubleClick(c);
    }

    @Override
    public void click(Component c, MouseButton button, int times) {
        robot.click(c, button, times);
    }

    @Override
    public void click(Component c, Point where) {
        robot.click(c, where);
    }

    @Override
    public void click(Component c, Point where, MouseButton button, int times) {
        robot.click(c, where, button, times);
    }

    @Override
    public void click(Point where, MouseButton button, int times) {
        robot.click(where, button, times);
    }

    @Override
    public void pressMouse(MouseButton button) {
        robot.pressMouse(button);
    }

    @Override
    public void pressMouseWhileRunning(MouseButton button, Runnable runnable) {
        robot.pressMouseWhileRunning(button, runnable);
    }

    @Override
    public void pressMouse(Component c, Point where) {
        robot.pressMouse(c, where);
    }

    @Override
    public void pressMouseWhileRunning(Component c, Point where, Runnable runnable) {
        robot.pressMouseWhileRunning(c, where, runnable);
    }

    @Override
    public void pressMouse(Component c, Point where, MouseButton button) {
        robot.pressMouse(c, where, button);
    }

    @Override
    public void pressMouseWhileRunning(Component c, Point where, MouseButton button, Runnable runnable) {
        robot.pressMouseWhileRunning(c, where, button, runnable);
    }

    @Override
    public void pressMouse(Point where, MouseButton button) {
        robot.pressMouse(where, button);
    }

    @Override
    public void pressMouseWhileRunning(Point where, MouseButton button, Runnable runnable) {
        robot.pressMouseWhileRunning(where, button, runnable);
    }

    @Override
    public void moveMouse(Component c) {
        robot.moveMouse(c);
    }

    @Override
    public void moveMouse(Component c, Point p) {
        robot.moveMouse(c, p);
    }

    @Override
    public void moveMouse(Component c, int x, int y) {
        robot.moveMouse(c, x, y);
    }

    @Override
    public void moveMouse(Point p) {
        robot.moveMouse(p);
    }

    @Override
    public void moveMouse(int x, int y) {
        robot.moveMouse(x, y);
    }

    @Override
    public void releaseMouse(MouseButton button) {
        robot.releaseMouse(button);
    }

    @Override
    public void releaseMouseButtons() {
        robot.releaseMouseButtons();
    }

    @Override
    public void rotateMouseWheel(Component c, int amount) {
        robot.rotateMouseWheel(c, amount);
    }

    @Override
    public void rotateMouseWheel(int amount) {
        robot.rotateMouseWheel(amount);
    }

    @Override
    public void jitter(Component c) {
        robot.jitter(c);
    }

    @Override
    public void jitter(Component c, Point where) {
        robot.jitter(c, where);
    }

    @Override
    public void enterText(String text) {
        robot.enterText(text);
    }

    @Override
    public void type(char character) {
        robot.type(character);
    }

    @Override
    public void pressAndReleaseKey(int keyCode, int... modifiers) {
        robot.pressAndReleaseKey(keyCode, modifiers);
    }

    @Override
    public void pressAndReleaseKeys(int... keyCodes) {
        robot.pressAndReleaseKeys(keyCodes);
    }

    @Override
    public void pressKey(int keyCode) {
        robot.pressKey(keyCode);
    }

    @Override
    public void pressKeyWhileRunning(int keyCode, Runnable runnable) {
        robot.pressKeyWhileRunning(keyCode, runnable);
    }

    @Override
    public void releaseKey(int keyCode) {
        robot.releaseKey(keyCode);
    }

    @Override
    public void pressModifiers(int modifierMask) {
        robot.pressModifiers(modifierMask);
    }

    @Override
    public void pressModifiersWhileRunning(int modifierMask, Runnable runnable) {
        robot.pressModifiersWhileRunning(modifierMask, runnable);
    }

    @Override
    public void releaseModifiers(int modifierMask) {
        robot.releaseModifiers(modifierMask);
    }

    @Override
    public void waitForIdle() {
        robot.waitForIdle();
    }

    @Override
    public boolean isDragging() {
        return robot.isDragging();
    }

    @Override
    public boolean isReadyForInput(Component c) {
        return robot.isReadyForInput(c);
    }

    @Override
    public JPopupMenu showPopupMenu(Component invoker) {
        return robot.showPopupMenu(invoker);
    }

    @Override
    public JPopupMenu showPopupMenu(Component invoker, Point location) {
        return robot.showPopupMenu(invoker, location);
    }

    @Override
    public JPopupMenu findActivePopupMenu() {
        return robot.findActivePopupMenu();
    }

    @Override
    public void requireNoJOptionPaneIsShowing() {
        robot.requireNoJOptionPaneIsShowing();
    }

    @Override
    public Settings settings() {
        return robot.settings();
    }

    @Override
    public boolean isActive() {
        return robot.isActive();
    }
}
