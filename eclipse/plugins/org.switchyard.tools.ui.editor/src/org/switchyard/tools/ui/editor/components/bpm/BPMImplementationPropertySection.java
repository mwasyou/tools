/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.switchyard.tools.ui.editor.components.bpm;

import java.math.BigInteger;

import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.platform.GFPropertySection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.soa.sca.sca1_1.model.sca.Component;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.switchyard.tools.models.switchyard1_0.bpm.ActionType1;
import org.switchyard.tools.models.switchyard1_0.bpm.BPMImplementationType;
import org.switchyard.tools.models.switchyard1_0.bpm.BPMPackage;
import org.switchyard.tools.ui.editor.Activator;

/**
 * BPMImplementationPropertySection
 * 
 * Property sheet composite for BPM component implementations.
 */
public class BPMImplementationPropertySection extends GFPropertySection implements ITabbedPropertyConstants {

    private TabbedPropertySheetPage _page;
    private Composite _panel;
    private Text _processIDText;
    private Text _sessionIDText;
    private Button _persistentButton;
    private BPMResourceTable _resourcesTable;
    private BPMActionTable _actionsTable;
    private BPMMappingsTable _inputsTable;
    private BPMMappingsTable _outputsTable;
    private BPMMappingsTable _globalsTable;
    private BPMPropertyTable _propertiesTable;
    private BPMLoggerTable _loggersTable;
    private BPMTaskHandlerTable _handlersTable;
    private BPMEventListenerTable _listenersTable;
    private BPMChannelTable _channelsTable;
    private BPMImplementationType _implementation;
    private boolean _updating;

    /**
     * Create a new BPMImplementationPropertySection.
     */
    public BPMImplementationPropertySection() {
    }

    @Override
    public void refresh() {
        _implementation = null;
        final PictogramElement pe = getSelectedPictogramElement();
        if (pe != null) {
            final Object bo = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe);
            _implementation = (BPMImplementationType) ((Component) bo).getImplementation();
        }
        _updating = true;
        try {
            _resourcesTable.setTargetObject(_implementation);
            _actionsTable.setTargetObject(_implementation);
            _propertiesTable.setTargetObject(_implementation);
            _loggersTable.setTargetObject(_implementation);
            _handlersTable.setTargetObject(_implementation);
            _listenersTable.setTargetObject(_implementation);
            _channelsTable.setTargetObject(_implementation);
            if (_implementation == null) {
                _processIDText.setText("");
                _sessionIDText.setText("");
                _persistentButton.setSelection(false);
            } else {
                _processIDText.setText(_implementation.getProcessId() == null ? "" : _implementation.getProcessId());
                _sessionIDText.setText(_implementation.getSessionId() == null ? "" : _implementation.getSessionId()
                        .toString());
                _persistentButton.setSelection(false);
            }
        } finally {
            _updating = false;
        }
    }

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage tabbedPropertySheetPage) {
        super.createControls(parent, tabbedPropertySheetPage);

        _page = tabbedPropertySheetPage;

        final TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
        _panel = factory.createComposite(parent);
        _panel.setLayout(new GridLayout());

        final TabFolder folder = new TabFolder(_panel, SWT.TOP);
        factory.adapt(folder);
        folder.setLayout(new Layout() {
            @Override
            protected void layout(Composite composite, boolean flushCache) {
                Rectangle rect = composite.getClientArea();

                TabItem[] selected = folder.getSelection();
                if (selected == null || selected.length == 0) {
                    return;
                }
                selected[0].getControl().setBounds(rect);
            }

            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
                    return new Point(wHint, hHint);
                }

                int maxWidth = 0, maxHeight = 0;
                TabItem[] selected = folder.getSelection();
                if (selected != null && selected.length > 0) {
                    Point pt = selected[0].getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
                    maxWidth = Math.max(0, pt.x);
                    maxHeight = Math.max(0, pt.y);
                }

                if (wHint != SWT.DEFAULT) {
                    maxWidth = wHint;
                }
                if (hHint != SWT.DEFAULT) {
                    maxHeight = hHint;
                }

                return new Point(maxWidth, maxHeight);
            }
        });
        folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TabItem general = new TabItem(folder, SWT.NONE);
        TabItem actions = new TabItem(folder, SWT.NONE);
        TabItem advanced = new TabItem(folder, SWT.NONE);

        general.setText("General");
        actions.setText("Actions");
        advanced.setText("Advanced");

        createGeneralControls(folder, general);
        createActionsControls(folder, actions);
        createAdvancedControls(folder, advanced);

        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                _page.resizeScrolledComposite();
            }
        });

        folder.setSelection(0);
    }

    private void createGeneralControls(TabFolder folder, TabItem item) {
        final TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
        Composite control = factory.createComposite(folder);
        control.setLayout(new GridLayout(2, false));
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        factory.createLabel(control, "Process ID:");
        _processIDText = factory.createText(control, "", SWT.NONE);
        _processIDText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        _processIDText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                final String newValue = _processIDText.getText().length() == 0 ? null : _processIDText.getText();
                if (!_updating
                        && ((newValue == null && _implementation.getProcessId() != null) || (newValue != null && !newValue
                                .equals(_implementation.getProcessId())))) {
                    wrapOperation(new Runnable() {
                        public void run() {
                            _implementation.setProcessId(newValue);
                        }
                    });
                }
            }
        });

        factory.createLabel(control, "Session ID:");
        _sessionIDText = factory.createText(control, "", SWT.NONE);
        _sessionIDText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        _sessionIDText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                final BigInteger newValue = _sessionIDText.getText().length() == 0 ? null : new BigInteger(
                        _sessionIDText.getText());
                if (!_updating
                        && ((newValue == null && _implementation.getSessionId() != null) || (newValue != null && !newValue
                                .equals(_implementation.getSessionId())))) {
                    wrapOperation(new Runnable() {
                        public void run() {
                            _implementation.setSessionId(newValue);
                        }
                    });
                }
            }
        });

        _persistentButton = factory.createButton(control, "Persistent", SWT.CHECK);
        _persistentButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        _persistentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!_updating && (_implementation.isPersistent() != _persistentButton.getSelection())) {
                    wrapOperation(new Runnable() {
                        public void run() {
                            _implementation.setPersistent(_persistentButton.getSelection());
                        }
                    });
                }
            }
        });

        ExpandableComposite resourcesSection = factory.createExpandableComposite(control, ExpandableComposite.TITLE_BAR
                | ExpandableComposite.EXPANDED);
        resourcesSection.setText("Resources");
        resourcesSection.setLayout(new GridLayout(2, false));
        resourcesSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        _resourcesTable = new BPMResourceTable(resourcesSection, SWT.NONE);
        resourcesSection.setClient(_resourcesTable);

        item.setControl(control);
    }

    private void createActionsControls(TabFolder folder, TabItem item) {
        final TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
        final Composite control = factory.createComposite(folder);
        control.setLayout(new GridLayout(2, false));
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite actionsComposite = factory.createComposite(control);
        actionsComposite.setLayout(new GridLayout());
        actionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ExpandableComposite actionsSection = factory.createExpandableComposite(actionsComposite,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
        actionsSection.setText("Actions");
        actionsSection.setLayout(new GridLayout());
        actionsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        _actionsTable = new BPMActionTable(actionsSection, SWT.NONE);
        actionsSection.setClient(_actionsTable);
        _actionsTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                final Object selected;
                if (event.getSelection().isEmpty()) {
                    selected = null;
                } else {
                    selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
                }
                _inputsTable.setTargetObject((ActionType1) selected);
                _outputsTable.setTargetObject((ActionType1) selected);
                _globalsTable.setTargetObject((ActionType1) selected);
            }
        });

        Composite mappingsComposite = factory.createComposite(control);
        mappingsComposite.setLayout(new GridLayout());
        mappingsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ExpandableComposite inputsSection = factory.createExpandableComposite(mappingsComposite,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
        inputsSection.setText("Inputs");
        inputsSection.setLayout(new GridLayout());
        inputsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        inputsSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _inputsTable = new BPMMappingsTable(inputsSection, SWT.NONE, BPMPackage.eINSTANCE.getActionType1_Inputs(),
                BPMPackage.eINSTANCE.getMappingsType_Mapping());
        inputsSection.setClient(_inputsTable);

        ExpandableComposite outputsSection = factory.createExpandableComposite(mappingsComposite,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
        outputsSection.setText("Outputs");
        outputsSection.setLayout(new GridLayout());
        outputsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        outputsSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _outputsTable = new BPMMappingsTable(outputsSection, SWT.NONE, BPMPackage.eINSTANCE.getActionType1_Outputs(),
                BPMPackage.eINSTANCE.getMappingsType_Mapping());
        outputsSection.setClient(_outputsTable);

        ExpandableComposite globalsSection = factory.createExpandableComposite(mappingsComposite,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        globalsSection.setText("Globals");
        globalsSection.setLayout(new GridLayout());
        globalsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        globalsSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _globalsTable = new BPMMappingsTable(globalsSection, SWT.NONE, BPMPackage.eINSTANCE.getActionType1_Globals(),
                BPMPackage.eINSTANCE.getMappingsType_Mapping());
        globalsSection.setClient(_globalsTable);

        item.setControl(control);
    }

    private void createAdvancedControls(TabFolder folder, TabItem item) {
        final TabbedPropertySheetWidgetFactory factory = getWidgetFactory();
        final Composite control = factory.createComposite(folder);
        control.setLayout(new GridLayout());
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ExpandableComposite propertiesSection = factory.createExpandableComposite(control, ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
        propertiesSection.setText("Properties");
        propertiesSection.setLayout(new GridLayout());
        propertiesSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        propertiesSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _propertiesTable = new BPMPropertyTable(propertiesSection, SWT.NONE);
        propertiesSection.setClient(_propertiesTable);

        ExpandableComposite loggersSection = factory.createExpandableComposite(control, ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR);
        loggersSection.setText("Audit Loggers");
        loggersSection.setLayout(new GridLayout());
        loggersSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        loggersSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _loggersTable = new BPMLoggerTable(loggersSection, SWT.NONE);
        loggersSection.setClient(_loggersTable);

        final ExpandableComposite handlersSection = factory.createExpandableComposite(control,
                ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        handlersSection.setText("Work Item Handlers");
        handlersSection.setLayout(new GridLayout());
        handlersSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        handlersSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _handlersTable = new BPMTaskHandlerTable(handlersSection, SWT.NONE);
        handlersSection.setClient(_handlersTable);

        ExpandableComposite listenersSection = factory.createExpandableComposite(control, ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR);
        listenersSection.setText("Event Listeners");
        listenersSection.setLayout(new GridLayout());
        listenersSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        listenersSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _listenersTable = new BPMEventListenerTable(listenersSection, SWT.NONE);
        listenersSection.setClient(_listenersTable);

        ExpandableComposite channelsSection = factory.createExpandableComposite(control, ExpandableComposite.TWISTIE
                | ExpandableComposite.TITLE_BAR);
        channelsSection.setText("Channels");
        channelsSection.setLayout(new GridLayout());
        channelsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        channelsSection.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                _page.resizeScrolledComposite();
            }
        });

        _channelsTable = new BPMChannelTable(channelsSection, SWT.NONE);
        channelsSection.setClient(_channelsTable);

        item.setControl(control);
    }

    private void wrapOperation(final Runnable runner) {
        TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(_implementation);
        if (domain != null) {
            domain.getCommandStack().execute(new RecordingCommand(domain) {
                @Override
                protected void doExecute() {
                    try {
                        runner.run();
                    } catch (Exception e) {
                        Activator.logError(e);
                    }
                }
            });
        } else {
            try {
                runner.run();
            } catch (Exception e) {
                Activator.logError(e);
            }
        }
    }
}