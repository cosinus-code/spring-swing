/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.context;

import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.Table;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
public class SwingFactory {

    private final SwingApplicationContext swingContext;

    public SwingFactory(SwingApplicationContext swingContext) {
        this.swingContext = swingContext;
    }

    public <S extends SwingInject> S newSwingContextInject(Class<S> swingClass) {
        try {
            return swingClass
                    .getConstructor(SwingApplicationContext.class)
                    .newInstance(swingContext);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new SwingInjectException(e);
        }
    }

    public <T, V, S extends SwingWorker<T, V>> S newSwingWorker(Class<S> swingWorkerClass) {
        return newSwingContextInject(swingWorkerClass);
    }

    public <S extends Table> S newSwingTable(Class<S> swingTableClass) {
        return newSwingContextInject(swingTableClass);
    }

    public <S extends Panel> S newSwingPanel(Class<S> swingPanelClass) {
        return newSwingContextInject(swingPanelClass);
    }

    public <T, S extends Dialog<T>> S newSwingDialog(Class<S> swingDialogClass) {
        return newSwingContextInject(swingDialogClass);
    }

    public <S extends org.cosinus.swing.form.Component> S newSwingComponent(Class<S> swingComponentClass) {
        return newSwingContextInject(swingComponentClass);
    }
}
