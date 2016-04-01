/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lsimedia.multiproperties.standalone;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import lsimedia.multiproperties.JMultiProperties;
import lsimedia.multiproperties.Logit;

/**
 *
 * @author sbodmer
 */
public class JMultiPropertiesFrame extends javax.swing.JFrame implements ActionListener, MouseListener, ListSelectionListener, ItemListener, FileFilter {

    /**
     * The current selected file
     */
    MultiProperties selected = null;

    /**
     * Current selected session
     */
    MultiPropertiesSession session = null;

    /**
     * The session (the first is the default one) and is always present
     */
    DefaultComboBoxModel<MultiPropertiesSession> sessions = new DefaultComboBoxModel<>();

    Logit logit = new Logit();

    /**
     * If gui is i lockdown mode
     */
    boolean lockdown = false;

    /**
     * Gui refresh timer
     */
    javax.swing.Timer timer = null;

    /**
     * Creates new form JMultiPropertiesFrame
     */
    public JMultiPropertiesFrame(boolean lockdown) {
        this.lockdown = lockdown;

        initComponents();

        MN_Quit.addActionListener(this);
        MN_Load.addActionListener(this);
        MN_SaveAll.addActionListener(this);

        BT_Load.addActionListener(this);
        BT_Close.addActionListener(this);
        BT_SaveAll.addActionListener(this);

        BT_Save.addActionListener(this);
        BT_SaveProcess.setVisible(!lockdown);
        BT_SaveProcess.addActionListener(this);

        PN_Title.setVisible(false);

        BT_NewSession.addActionListener(this);
        BT_EditSession.addActionListener(this);
        BT_DeleteSession.addActionListener(this);

        //--- Load properties and default session
        try {
            File d = new File(System.getProperty("user.home"), ".multiproperties");
            d.mkdirs();

            File f = new File(d, "jmultiproperties.properties");
            FileInputStream fin = new FileInputStream(f);
            Properties prop = new Properties();
            prop.load(fin);
            fin.close();

            int width = Integer.parseInt(prop.getProperty("width", "1024"));
            int height = Integer.parseInt(prop.getProperty("height", "768"));
            int divider = Integer.parseInt(prop.getProperty("divider", "200"));

            setSize(new Dimension(width, height));
            setLocationByPlatform(true);
            SP_Main.setDividerLocation(divider);

            //--- Load all the sessions
            File s[] = d.listFiles();
            for (int i = 0;i < s.length;i++) {
                File tmp = s[i];
                String name = tmp.getName();
                if (tmp.isFile() && name.startsWith("session_") && name.endsWith(".properties")) {
                    //--- Find the identifier
                    name = name.substring(8);
                    name = name.substring(0, name.length() - 11);
                    // System.out.println(">>NAME:" + name);
                    MultiPropertiesSession se = loadSession(name);
                    sessions.addElement(se);

                    //--- If default, set it
                    if (se.getIdentifier().equals("default")) session = se;

                }
            }

        } catch (Exception ex) {
            //---
        }

        //--- Check if default session exists
        if (session == null) {
            session = loadSession("default");
            sessions.addElement(session);
            logit.log("M", "Default session created", null);
        }

        //---
        CMB_Sessions.setModel((ComboBoxModel) sessions);
        CMB_Sessions.setSelectedItem(session);
        CMB_Sessions.addItemListener(this);

        //--- Set default session
        LI_Files.setModel((ListModel) session.getModel());
        LI_Files.setCellRenderer(new JMultiPropertiesCellRenderer());
        LI_Files.addMouseListener(this);
        LI_Files.addListSelectionListener(this);

        timer = new javax.swing.Timer(1000, this);
        timer.start();
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            //--- Print the logs
            while (true) {
                String s[] = logit.fetchLog();
                if (s == null) break;

                String txt = s[0] + " (" + s[1] + ") " + s[2];
                TA_Logit.append(txt + "\n");
                LB_Status.setText(txt);
            }

            boolean enabled = false;
            DefaultListModel<MultiProperties> model = session.getModel();
            for (int i = 0;i < model.size();i++) {
                MultiProperties cont = model.get(i);
                JMultiProperties jm = cont.getVisual();
                if (jm != null) {
                    if (jm.isModified()) enabled = true;
                }
            }
            MN_SaveAll.setEnabled(enabled);
            BT_SaveAll.setEnabled(enabled);
            CMB_Sessions.setEnabled(!enabled);
            LI_Files.repaint();

            if (session.getIdentifier().equals("default")) {
                BT_EditSession.setEnabled(false);
                BT_DeleteSession.setEnabled(false);

            } else {
                BT_EditSession.setEnabled(true);
                BT_DeleteSession.setEnabled(true);
            }
        } else if (e.getActionCommand().equals("quit")) {
            close();

        } else if (e.getActionCommand().equals("load")) {
            JFileChooser jf = new JFileChooser(session.getLastFile());
            jf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            jf.setMultiSelectionEnabled(true);
            jf.setFileFilter(new FileNameExtensionFilter("Multiproperties", "multiproperties"));
            int rep = jf.showOpenDialog(this);
            if (rep == JFileChooser.APPROVE_OPTION) {
                DefaultListModel<MultiProperties> model = session.getModel();
                File f[] = jf.getSelectedFiles();
                for (int i = 0;i < f.length;i++) {
                    File file = f[i];
                    if (file.isFile()) {
                        MultiProperties cont = new MultiProperties(file);
                        model.addElement(cont);
                        logit.log("I", file.getPath() + " loaded", null);

                    } else if (file.isDirectory()) {
                        ArrayList<File> stack = new ArrayList<>();
                        stackFiles(file, stack, this);
                        for (int j = 0;j < stack.size();j++) {
                            MultiProperties cont = new MultiProperties(stack.get(j));
                            model.addElement(cont);
                            logit.log("I", file.getPath() + " loaded", null);

                        }

                    }
                    session.setLastFile(file);
                }
            }

        } else if (e.getActionCommand().equals("save")) {
            if (selected != null) {
                JMultiProperties jm = selected.getVisual();
                if (jm != null) {
                    jm.save(false);
                    logit.log("M", "" + selected.getFile().getPath() + " saved", null);
                }

            }

        } else if (e.getActionCommand().equals("saveProcess")) {
            if (selected != null) {
                JMultiProperties jm = selected.getVisual();
                if (jm != null) {
                    jm.save(true);
                    logit.log("M", "" + selected.getFile().getPath() + " saved and processed", null);
                }

            }

        } else if (e.getActionCommand().equals("saveAll")) {
            //---
            DefaultListModel<MultiProperties> model = session.getModel();
            for (int i = 0;i < model.size();i++) {
                MultiProperties cont = model.get(i);
                JMultiProperties jm = cont.getVisual();
                if (jm != null) jm.save(false);

            }
            LI_Files.repaint();
            MN_SaveAll.setEnabled(false);
            BT_SaveAll.setEnabled(false);

        } else if (e.getActionCommand().equals("close")) {
            int indices[] = LI_Files.getSelectedIndices();
            DefaultListModel<MultiProperties> model = session.getModel();
            ArrayList<MultiProperties> toClose = new ArrayList<>();
            for (int i = 0;i < indices.length;i++) toClose.add(model.get(indices[i]));
            for (int i = 0;i < toClose.size();i++) {
                MultiProperties cont = toClose.get(i);
                JMultiProperties jm = cont.getVisual();
                if (jm != null) {
                    PN_Content.remove(jm);
                    PN_Content.revalidate();
                    if (cont == selected) {
                        selected = null;
                        CardLayout layout = (CardLayout) PN_Content.getLayout();
                        layout.show(PN_Content, "empty");
                        PN_Title.setVisible(false);
                    }
                }
                model.removeElement(cont);
            }

        } else if (e.getActionCommand().equals("newSession")) {
            String title = JOptionPane.showInputDialog(this, "Title");
            if (title != null) {
                session = new MultiPropertiesSession(title, null);
                sessions.addElement(session);
                CMB_Sessions.setSelectedItem(session);
                // LI_Files.setModel((ListModel) session.getModel());
            }

        } else if (e.getActionCommand().equals("editSession")) {
            if (session.getIdentifier().equals("default")) return;

            String title = JOptionPane.showInputDialog(this, "Title", session.getTitle());
            if (title != null) session.setTitle(title);

        } else if (e.getActionCommand().equals("removeSession")) {
            if (session.getIdentifier().equals("default")) return;

            int rep = JOptionPane.showConfirmDialog(this, "Do you really want to remove the session ?", "Remove", JOptionPane.YES_NO_OPTION);
            if (rep == JOptionPane.YES_OPTION) {
                DefaultListModel<MultiProperties> model = session.getModel();
                for (int i = 0;i < model.getSize();i++) {
                    MultiProperties cont = model.getElementAt(i);
                    JMultiProperties jm = cont.getVisual();
                    if (jm != null) {
                        PN_Content.remove(jm);
                        PN_Content.revalidate();
                        if (cont == selected) {
                            selected = null;
                            CardLayout layout = (CardLayout) PN_Content.getLayout();
                            layout.show(PN_Content, "empty");
                            PN_Title.setVisible(false);
                        }
                        cont.setVisual(null);
                    }
                }
                
                //--- Delete session file
                try {
                    File d = new File(System.getProperty("user.home"), ".multiproperties");
                    String identifier = session.getIdentifier();
                    File f = new File(d, "session_" + identifier + ".properties");
                    f.delete();
                    
                } catch (Exception ex) {
                    //---
               
                }

                //--- The itemListener will select the new one
                sessions.removeElement(session);

            }
        }
    }

    //**************************************************************************
    //*** NMouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == LI_Files) {
            if (e.getClickCount() >= 2) {
                DefaultListModel<MultiProperties> model = session.getModel();
                MultiProperties cont = model.elementAt(LI_Files.getSelectedIndex());
                LB_File.setText(cont.getFile().getName());
                LB_Path.setText(cont.getFile().getParent());
                PN_Title.setVisible(true);

                selected = cont;
                if (selected.getVisual() == null) {
                    JMultiProperties jm = new JMultiProperties(logit, lockdown);
                    jm.setFile(cont.getFile());
                    selected.setVisual(jm);
                    PN_Content.add(jm, cont.getFile().getPath());

                }

                CardLayout layout = (CardLayout) PN_Content.getLayout();
                layout.show(PN_Content, cont.getFile().getPath());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //---
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //---
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //---
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //---
    }

    //**************************************************************************
    //*** ListSelectionListener
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == LI_Files) {
            if (e.getValueIsAdjusting() == false) {
                BT_Close.setEnabled(LI_Files.getSelectedIndex() >= 0);
            }
        }
    }

    //**************************************************************************
    //*** FileFilter
    //**************************************************************************
    @Override
    public boolean accept(File pathname) {
        if (pathname.isFile()) {
            if (pathname.getName().toLowerCase().endsWith(".multiproperties")) return true;

        } else if (pathname.isDirectory()) {
            return true;
        }
        return false;
    }

    //**************************************************************************
    //*** ItemListener
    //**************************************************************************
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            session = (MultiPropertiesSession) e.getItem();
            LI_Files.setModel((ListModel) session.getModel());

        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            MultiPropertiesSession se = (MultiPropertiesSession) e.getItem();
            DefaultListModel<MultiProperties> model = se.getModel();
            for (int i = 0;i < model.getSize();i++) {
                MultiProperties cont = model.getElementAt(i);
                JMultiProperties jm = cont.getVisual();
                if (jm != null) {
                    PN_Content.remove(jm);
                    PN_Content.revalidate();
                    if (cont == selected) {
                        selected = null;
                        CardLayout layout = (CardLayout) PN_Content.getLayout();
                        layout.show(PN_Content, "empty");
                        PN_Title.setVisible(false);
                    }
                    cont.setVisual(null);
                }
            }

        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        LB_Status = new javax.swing.JLabel();
        SP_Main = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        LI_Files = new javax.swing.JList<>();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TA_Logit = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jToolBar3 = new javax.swing.JToolBar();
        CMB_Sessions = new javax.swing.JComboBox<>();
        BT_NewSession = new javax.swing.JButton();
        BT_EditSession = new javax.swing.JButton();
        BT_DeleteSession = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        PN_Content = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        PN_Title = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        BT_Save = new javax.swing.JButton();
        BT_SaveProcess = new javax.swing.JButton();
        LB_File = new javax.swing.JLabel();
        LB_Path = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        BT_Load = new javax.swing.JButton();
        BT_SaveAll = new javax.swing.JButton();
        BT_Close = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        MN_Load = new javax.swing.JMenuItem();
        MN_SaveAll = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        MN_Quit = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        LB_Status.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        LB_Status.setText("...");
        jPanel1.add(LB_Status);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        SP_Main.setDividerLocation(200);

        jPanel2.setLayout(new java.awt.BorderLayout());

        LI_Files.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        LI_Files.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jScrollPane2.setViewportView(LI_Files);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel5.setPreferredSize(new java.awt.Dimension(143, 200));
        jPanel5.setLayout(new java.awt.BorderLayout());

        TA_Logit.setEditable(false);
        TA_Logit.setColumns(20);
        TA_Logit.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Logit.setLineWrap(true);
        TA_Logit.setRows(5);
        jScrollPane1.setViewportView(TA_Logit);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5, java.awt.BorderLayout.SOUTH);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jToolBar3.setFloatable(false);
        jToolBar3.setRollover(true);

        CMB_Sessions.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jToolBar3.add(CMB_Sessions);

        BT_NewSession.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_NewSession.setText("New");
        BT_NewSession.setActionCommand("newSession");
        BT_NewSession.setFocusable(false);
        BT_NewSession.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_NewSession.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(BT_NewSession);

        BT_EditSession.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_EditSession.setText("Edit");
        BT_EditSession.setActionCommand("editSession");
        BT_EditSession.setFocusable(false);
        BT_EditSession.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_EditSession.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(BT_EditSession);

        BT_DeleteSession.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_DeleteSession.setText("Delete");
        BT_DeleteSession.setActionCommand("removeSession");
        BT_DeleteSession.setFocusable(false);
        BT_DeleteSession.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_DeleteSession.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar3.add(BT_DeleteSession);

        jPanel4.add(jToolBar3, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel4, java.awt.BorderLayout.PAGE_START);

        SP_Main.setLeftComponent(jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());

        PN_Content.setLayout(new java.awt.CardLayout());
        PN_Content.add(jLabel1, "empty");

        jPanel3.add(PN_Content, java.awt.BorderLayout.CENTER);

        PN_Title.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        BT_Save.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Save.setText("Save");
        BT_Save.setActionCommand("save");
        BT_Save.setFocusable(false);
        BT_Save.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Save.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(BT_Save);

        BT_SaveProcess.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_SaveProcess.setText("Save and process");
        BT_SaveProcess.setActionCommand("saveProcess");
        BT_SaveProcess.setFocusable(false);
        BT_SaveProcess.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_SaveProcess.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(BT_SaveProcess);

        PN_Title.add(jToolBar2);

        LB_File.setFont(new java.awt.Font("Arial", 1, 11)); // NOI18N
        LB_File.setText("...");
        PN_Title.add(LB_File);

        LB_Path.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        LB_Path.setText("...");
        PN_Title.add(LB_Path);

        jPanel3.add(PN_Title, java.awt.BorderLayout.NORTH);

        SP_Main.setRightComponent(jPanel3);

        getContentPane().add(SP_Main, java.awt.BorderLayout.CENTER);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        BT_Load.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Load.setText("Load");
        BT_Load.setActionCommand("load");
        BT_Load.setFocusable(false);
        BT_Load.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Load.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Load);

        BT_SaveAll.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_SaveAll.setText("Save All");
        BT_SaveAll.setActionCommand("saveAll");
        BT_SaveAll.setEnabled(false);
        BT_SaveAll.setFocusable(false);
        BT_SaveAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_SaveAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_SaveAll);

        BT_Close.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Close.setText("Close");
        BT_Close.setActionCommand("close");
        BT_Close.setEnabled(false);
        BT_Close.setFocusable(false);
        BT_Close.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Close.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Close);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jMenuBar1.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        jMenu1.setText("File");
        jMenu1.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N

        MN_Load.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_Load.setText("Load");
        MN_Load.setActionCommand("load");
        jMenu1.add(MN_Load);

        MN_SaveAll.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_SaveAll.setText("Save all");
        MN_SaveAll.setActionCommand("saveAll");
        MN_SaveAll.setEnabled(false);
        jMenu1.add(MN_SaveAll);
        jMenu1.add(jSeparator1);

        MN_Quit.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MN_Quit.setText("Quit");
        MN_Quit.setActionCommand("quit");
        jMenu1.add(MN_Quit);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        setBounds(0, 0, 810, 630);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        close();
    }//GEN-LAST:event_formWindowClosing


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_Close;
    private javax.swing.JButton BT_DeleteSession;
    private javax.swing.JButton BT_EditSession;
    private javax.swing.JButton BT_Load;
    private javax.swing.JButton BT_NewSession;
    private javax.swing.JButton BT_Save;
    private javax.swing.JButton BT_SaveAll;
    private javax.swing.JButton BT_SaveProcess;
    private javax.swing.JComboBox<String> CMB_Sessions;
    private javax.swing.JLabel LB_File;
    private javax.swing.JLabel LB_Path;
    private javax.swing.JLabel LB_Status;
    private javax.swing.JList<String> LI_Files;
    private javax.swing.JMenuItem MN_Load;
    private javax.swing.JMenuItem MN_Quit;
    private javax.swing.JMenuItem MN_SaveAll;
    private javax.swing.JPanel PN_Content;
    private javax.swing.JPanel PN_Title;
    private javax.swing.JSplitPane SP_Main;
    private javax.swing.JTextArea TA_Logit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    // End of variables declaration//GEN-END:variables

    private void close() {
        if (BT_SaveAll.isEnabled()) {
            int rep = JOptionPane.showConfirmDialog(this, "Not all files are saved\n\nDo you really want to quit applications ?", "Quit", JOptionPane.YES_NO_OPTION);
            if (rep == JOptionPane.NO_OPTION) return;

        }

        timer.stop();

        //--- Save properties
        try {
            File d = new File(System.getProperty("user.home"), ".multiproperties");
            File f = new File(d, "jmultiproperties.properties");
            Properties prop = new Properties();
            FileOutputStream fout = new FileOutputStream(f);
            prop.put("width", "" + getWidth());
            prop.put("height", "" + getHeight());
            prop.put("divider", "" + SP_Main.getDividerLocation());

            prop.store(fout, "JMultiPropertiesFrame");
            fout.flush();
            fout.close();

        } catch (Exception ex) {
            //---
        }

        //--- Save last session
        saveSession(session);

        setVisible(false);
        dispose();

    }

    /**
     * Return true on sucess, the session are save in the user home under
     * .multiproperties and the file name is "session_{identifier}.properties"
     * <p>
     *
     *
     * @param session
     * @return
     */
    private boolean saveSession(MultiPropertiesSession session) {
        //--- Save properties
        try {
            File d = new File(System.getProperty("user.home"), ".multiproperties");
            String identifier = session.getIdentifier();
            File f = new File(d, "session_" + identifier + ".properties");
            FileOutputStream fout = new FileOutputStream(f);

            Properties prop = new Properties();
            prop.put("Title", session.getTitle());

            //--- Store list of opened file
            DefaultListModel<MultiProperties> model = session.getModel();
            for (int i = 0;i < model.size();i++) {
                MultiProperties cont = model.get(i);
                prop.put("File" + i, cont.getFile().getPath());
            }
            prop.put("lastFile", session.getLastFile().getPath());

            prop.store(fout, "JMultiPropertiesFrame Session");
            fout.flush();
            fout.close();

            return true;

        } catch (Exception ex) {
            //---
        }
        return false;
    }

    /**
     * Load the session, always return an object
     *
     * @param identifier
     * @return
     */
    private MultiPropertiesSession loadSession(final String identifier) {
        MultiPropertiesSession session = new MultiPropertiesSession(identifier, identifier);
        DefaultListModel model = session.getModel();
        try {
            //--- Load stored default session file for default session
            File d = new File(System.getProperty("user.home"), ".multiproperties");
            File f = new File(d, "session_" + identifier + ".properties");
            FileInputStream fin = new FileInputStream(f);
            Properties prop = new Properties();
            prop.load(fin);
            fin.close();

            session.setTitle(prop.getProperty("Title", identifier));

            int i = 0;
            while (true) {
                String path = prop.getProperty("File" + i);
                if (path == null) break;

                File file = new File(path);
                if (file.exists()) {
                    MultiProperties cont = new MultiProperties(file);
                    model.addElement(cont);
                    logit.log("M", "" + file.getName() + " added", null);

                } else {
                    logit.log("E", "" + file.getName() + " does not exist anymore", null);

                }

                i++;
            }

            session.setLastFile(new File(prop.getProperty("lastFile", "")));
            
            logit.log("M", "Session " + session.getTitle() + " loaded", null);

        } catch (Exception ex) {
            //---

        }
        return session;
    }

    /**
     * Stack the file recursively in a vector
     */
    private void stackFiles(File file, ArrayList<File> stack, FileFilter filter) {
        if (file.isDirectory()) {
            File list[] = file.listFiles(filter);
            for (int i = 0;i < list.length;i++) stackFiles(list[i], stack, filter);
            list = null;

        } else {
            stack.add(file);

        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
 /*
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info:javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JMultiPropertiesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JMultiPropertiesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JMultiPropertiesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JMultiPropertiesFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
         */
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                boolean lockdown = false;

                for (int i = 0;i < args.length;i++) {
                    if (args[i].equals("-lockdown")) lockdown = true;
                }

                new JMultiPropertiesFrame(lockdown).setVisible(true);
            }
        });
    }

}
