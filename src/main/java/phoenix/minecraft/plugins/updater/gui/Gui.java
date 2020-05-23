package phoenix.minecraft.plugins.updater.gui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import phoenix.minecraft.plugins.updater.header.Header;

/**
 * for (Map.Entry<?, ?> e : System.getProperties().entrySet()) {
 * System.out.println(String.format("%s = %s", e.getKey(), e.getValue())); }
 */
public class Gui extends JFrame {

    private static final long serialVersionUID = 1L;

    static JTextArea ta_error;
    static JTextArea ta_msg;

    boolean debug = false;

    static CopyOnWriteArrayList<String> flansFileUrls = new CopyOnWriteArrayList<>();

    public static void followWebDir(URL url) throws IOException {
        //  System.out.println("followWebDir for : " + url);
        Document doc = Jsoup.connect(url.toString()).get();
        Elements selected = doc.select("a[href]");

        for (Element file : selected) {
            String item = file.attr("href");
            if (item.contains(";")) {
                continue;
            }
            if (item.startsWith("/")) {
                continue;
            }
            if (item.toLowerCase().endsWith("/")) {
                String fullpath = url.toString() + item;
                followWebDir(new URL(fullpath));
            } else {
                String fullpath = url.toString() + item;
                flansFileUrls.add(fullpath);
            }
        }
    }

    private void clearMessages() {
        ta_error.setText("");
        ta_msg.setText("");
    }

    private void installFlans() {

        //FLANS
        try {
            URL url = new URL(Header.flans);
            followWebDir(url);

            flansFileUrls.forEach((fullpath) -> {
                try {
                    String forlocal = fullpath.replace(Header.flans, "");

                    RemoteLocaleFilePair rlfp = new RemoteLocaleFilePair();
                    rlfp.setRoot(Header.minecarftFlansDir);
                    rlfp.setRemote(new URL(fullpath));
                    rlfp.setLocaleFile(new File(rlfp.getRoot(), forlocal));

                    if (!rlfp.localeFile.exists()) {
                        File parent = rlfp.getLocaleFile().getParentFile();
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                        try {
                            FileUtils.copyURLToFile(
                                    rlfp.remote,
                                    rlfp.localeFile,
                                    30000,
                                    30000);
                            String msg = rlfp.remote + " загружен";
                            System.out.println(msg);
                            ta_msg.append(msg + "\n");
                        } catch (IOException ex) {
                            Logger.getLogger(Gui.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("Flans pack: " + rlfp.localeFile + " уже установлен");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        //FLANS

    }

    private void complete() {
        String msg_complete;
        if (ta_error.getText().isEmpty()) {
            msg_complete = "Установка mods завершена без ошибок";
        } else {
            msg_complete = "Установка mods завершена c ошибками (смотрите справа)";

        }
        // output
        System.out.println(msg_complete);
        ta_msg.append('\n' + msg_complete);
    }

    class RemoteLocaleFilePair {

        @Getter
        @Setter
        private URL remote;
        @Getter
        @Setter
        private File root;
        @Getter
        @Setter
        private File localeFile;

    }

    public static void initDirs() {
        if (Header.OS.contains("WIN")) {
            Header.workingDirectory = System.getenv("AppData");
            File tmpFile = new File(Header.workingDirectory);
            Header.minecarftDir = new File(tmpFile, ".minecraft");
        } else {
            Header.workingDirectory = System.getProperty("user.home");
            Header.minecarftDir = new File(Header.workingDirectory, ".minecraft");
        }
        Header.minecarftModsDir = new File(Header.minecarftDir, "mods");
        Header.minecarftFlansDir = new File(Header.minecarftDir, "Flan");

        if (!Header.minecarftFlansDir.exists()) {
            Header.minecarftFlansDir.mkdirs();
        }
        if (!Header.minecarftModsDir.exists()) {
            Header.minecarftModsDir.mkdirs();
        }
    }

    public static void installMods() {
        //MODS
        try {
            URL listModesUrl = new URL(Header.mods);
            Document document = Jsoup.connect(listModesUrl.toString()).get();
            Elements links = document.select("a[href]");
            ArrayList<String> urls = new ArrayList<>();
            for (Element link : links) {
                String fileName = link.attr("href");
                if (new File(fileName).getName().endsWith(".jar")) {
                    String url = listModesUrl + fileName;
                    urls.add(url);
                }
            }
            File[] localMods = Header.minecarftModsDir.listFiles();
            removeNotContains(localMods, urls);
            back:
            {
                for (final String url : urls) {
                    final File fileToSave = new File(Header.minecarftModsDir, new File(url).getName());
                    if (!fileToSave.exists()) {
                        try {
                            Thread.currentThread().setName(url + " ---> " + fileToSave.getName());
                            URL urlToDownload = new URL(url);

                            FileUtils.copyURLToFile(urlToDownload, fileToSave, 10000, 5000);
                            String msg = "\n" + urlToDownload + " загружен";
                            ta_msg.append(msg);
                            System.out.println(msg.replaceAll("\n", ""));
                        } catch (Exception e2) {
                            ta_error.append("\n" + "FILE:" + fileToSave
                                    + "не коррекно загружен(повторите снова)" + '\n' + ExceptionUtils.getStackTrace(e2));
                            fileToSave.delete();
                            e2.printStackTrace();
                            break back;
                        }
                    }
                }
            }
        } catch (IOException e3) {
            ta_error.append('\n' + ExceptionUtils.getStackTrace(e3));
            e3.printStackTrace();
        }

    }

    public static void printCheckBanner() {
        File minecraftdir = Header.minecarftDir;

        String msg1 = "minecarftDir:" + minecraftdir.getAbsolutePath() + ":"
                + minecraftdir.exists() + '\n';
        String msg2_mods = "ModsDir:" + Header.minecarftModsDir.getAbsolutePath() + ":"
                + Header.minecarftModsDir.exists() + '\n';
        String msg2_flans = "FlansPackDir:" + Header.minecarftFlansDir.getAbsolutePath() + ":"
                + Header.minecarftFlansDir.exists() + '\n';

        String msg3 = "######################\n";

        ta_msg.append("OS:" + Header.OS + '\n');
        System.err.println(msg1);
        ta_msg.append(msg1);
        System.err.println(msg2_mods);
        ta_msg.append(msg2_mods);

        System.err.println(msg2_flans);
        ta_msg.append(msg2_flans);

        System.err.println(msg3);
        ta_msg.append(msg3);
    }

    public Gui() {

        if (debug) {
            Header header = new Header();
            Field[] field = header.getClass().getFields();
            try {
                for (Field field1 : field) {
                    System.out.println(field1.getName() + "\t:\t" + header.getClass().getField(field1.getName()).get(header));
                }
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }

        setBounds(new Rectangle(800, 420));
        setResizable(true);
        setTitle("Установщик модов Skanfa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setAlwaysOnTop(true);
        SpringLayout springLayout = new SpringLayout();
        getContentPane().setLayout(springLayout);

        final JButton btnNewButton = new JButton("Установить");
        btnNewButton.addActionListener((ActionEvent e) -> {
            new Thread(() -> {
                btnNewButton.setEnabled(false);
                try {

                    initDirs();
                    clearMessages();
                    printCheckBanner();

                    installFlans();
                    installMods();

                    complete();

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                btnNewButton.setEnabled(true);
            }).start();
        });

        springLayout.putConstraint(SpringLayout.NORTH, btnNewButton, 54, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, btnNewButton, 224, SpringLayout.WEST, getContentPane());
        getContentPane().add(btnNewButton);

        JScrollPane scrollPane_msg = new JScrollPane();
        springLayout.putConstraint(SpringLayout.NORTH, scrollPane_msg, 131, SpringLayout.NORTH, getContentPane());
        springLayout.putConstraint(SpringLayout.WEST, scrollPane_msg, 24, SpringLayout.WEST, getContentPane());
        springLayout.putConstraint(SpringLayout.SOUTH, scrollPane_msg, -25, SpringLayout.SOUTH, getContentPane());
        getContentPane().add(scrollPane_msg);

        JScrollPane scrollPane_error = new JScrollPane();
        springLayout.putConstraint(SpringLayout.WEST, scrollPane_error, 414, SpringLayout.WEST, scrollPane_msg);
        springLayout.putConstraint(SpringLayout.EAST, scrollPane_msg, -10, SpringLayout.WEST, scrollPane_error);
        springLayout.putConstraint(SpringLayout.EAST, scrollPane_error, -36, SpringLayout.EAST, getContentPane());
        springLayout.putConstraint(SpringLayout.NORTH, scrollPane_error, 52, SpringLayout.SOUTH, btnNewButton);
        springLayout.putConstraint(SpringLayout.SOUTH, scrollPane_error, -25, SpringLayout.SOUTH, getContentPane());

        ta_msg = new JTextArea();
        scrollPane_msg.setViewportView(ta_msg);
        getContentPane().add(scrollPane_error);

        ta_error = new JTextArea();
        scrollPane_error.setViewportView(ta_error);

    }

    protected boolean threadIsRun(CopyOnWriteArrayList<Thread> threads) {
        if (threads.size() == 0) {
            return false;
        }
        for (Thread thread : threads) {
            if (thread.getState().equals(Thread.State.TERMINATED)) {
                threads.remove(thread);
                String msg = "Поток " + thread + " завершил работу";
                System.out.println(msg);
                ta_msg.append('\n' + msg);
            }
        }
        return true;
    }

    public static void removeNotContains(File[] localMods, ArrayList<String> urls) {
        for (File file : localMods) {
            if (!fileContainInList(file, urls)) {
                file.delete();
            }
        }
    }

    public static boolean fileContainInList(File file, ArrayList<String> urls) {
        for (String string : urls) {
            String nameString = new File(string).getName();
            if (file.getName().equals(nameString)) {
                String msg = "mod: " + file.getAbsolutePath() + " уже установлен";
                System.out.println(msg);
                ta_msg.append(msg + '\n');
                return true;
            }
        }
        System.err.println(file.getAbsolutePath());
        return false;
    }
}
