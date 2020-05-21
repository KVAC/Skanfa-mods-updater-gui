package phoenix.minecraft.plugins.updater.gui;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import phoenix.minecraft.plugins.updater.header.Header;

public class Gui extends JFrame {

    private static final long serialVersionUID = 1L;

    private String OS = (System.getProperty("os.name")).toUpperCase();

    JTextArea ta_error;
    static JTextArea ta_msg;

    public Gui() {
        setBounds(new Rectangle(800, 420));
        setResizable(true);
        setTitle("Установщик модов Skanfa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // setAlwaysOnTop(true);
        SpringLayout springLayout = new SpringLayout();
        getContentPane().setLayout(springLayout);

        final JButton btnNewButton = new JButton("Установить");
        btnNewButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        btnNewButton.setEnabled(false);

                        try {
                            ta_error.setText("");
                            ta_msg.setText("");
                            URL listModesUrl = new URL(Header.SiteModeLink);

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

                            /*
							 * for (Map.Entry<?, ?> e : System.getProperties().entrySet()) {
							 * System.out.println(String.format("%s = %s", e.getKey(), e.getValue())); }
                             */
                            String workingDirectory;
                            File minecarftDir = null;
                            final File minecarftModsDir;

                            if (OS.contains("WIN")) {

                                workingDirectory = System.getenv("AppData");

                                File tmpFile = new File(workingDirectory);
                                // tmpFile = new File(tmpFile, "Roaming"); // Roaming

                                minecarftDir = new File(tmpFile, ".minecraft");
                            } else {
                                workingDirectory = System.getProperty("user.home");
                                minecarftDir = new File(workingDirectory, ".minecraft");
                            }
                            ta_msg.append("OS:" + OS + '\n');

                            minecarftModsDir = new File(minecarftDir, "mods");
                            if (!minecarftModsDir.exists()) {
                                minecarftModsDir.mkdirs();
                            }

                            String msg1 = "minecarftDir:" + minecarftDir.getAbsolutePath() + ":"
                                    + minecarftDir.exists();
                            String msg2 = "ModsDir:" + minecarftModsDir.getAbsolutePath() + ":"
                                    + minecarftModsDir.exists();
                            System.err.println(msg1);
                            ta_msg.append(msg1 + '\n');
                            System.err.println(msg2);
                            ta_msg.append(msg2 + '\n');

                            ta_msg.append("######################\n");
                            File[] localMods = minecarftModsDir.listFiles();
                            removeNotContains(localMods, urls);

                            back:
                            {
                                for (final String url : urls) {
                                    final File fileToSave = new File(minecarftModsDir, new File(url).getName());

                                    if (!fileToSave.exists()) {
                                        try {
                                            Thread.currentThread().setName(url + " ---> " + fileToSave.getName());

                                            FileUtils.copyURLToFile(new URL(url), fileToSave, 10000, 5000);
                                            String msg = '\n' + fileToSave.getName() + " загружен";
                                            ta_msg.append(msg);
                                            System.out.println(msg.replaceAll("\n", ""));
                                        } catch (Exception e) {
                                            ta_error.append("\n" + "FILE:" + fileToSave
                                                    + "не коррекно загружен(повторите снова)" + '\n'
                                                    + ExceptionUtils.getStackTrace(e));
                                            fileToSave.delete();
                                            e.printStackTrace();
                                            break back;
                                        }
                                    }
                                }
                            }
                            String msg_complete;
                            if (ta_error.getText().isEmpty()) {
                                msg_complete = "Установка завершена без ошибок";
                            } else {
                                msg_complete = "Установка завершена c ошибками (смотрите справа)";

                            }
                            // output
                            System.out.println(msg_complete);
                            ta_msg.append('\n' + msg_complete);
                        } catch (Exception e1) {
                            ta_error.append('\n' + ExceptionUtils.getStackTrace(e1));
                            e1.printStackTrace();
                        }
                        btnNewButton.setEnabled(true);
                    }
                }).start();
            }
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
        for (int i = 0; i < localMods.length; i++) {
            File file = localMods[i];
            if (!fileContainInList(file, urls)) {
                file.delete();
            }
        }
    }

    public static boolean fileContainInList(File file, ArrayList<String> urls) {
        for (String string : urls) {
            String nameString = new File(string).getName();
            if (file.getName().equals(nameString)) {
                String msg = nameString + " уже установлен";
                System.out.println(msg);
                ta_msg.append(msg + '\n');
                return true;
            }
        }
        System.err.println(file.getAbsolutePath());
        return false;
    }
}
