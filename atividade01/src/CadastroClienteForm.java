import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.text.ParseException;

public class CadastroClienteForm extends JFrame {

    private JTextField txtNome;
    private JTextField txtEmail;
    private JFormattedTextField txtTelefone;
    private JTextField txtCpf;

    private JButton btnSalvar;
    private JButton btnLimpar;
    private JButton btnListar;

    public CadastroClienteForm() {

        setTitle("Cadastro de Cliente");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nome:"), gbc);

        gbc.gridx = 1;
        txtNome = new JTextField(20);
        add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("E-mail:"), gbc);

        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Telefone:"), gbc);

        gbc.gridx = 1;
        txtTelefone = criarMascaraTelefone();
        add(txtTelefone, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("CPF:"), gbc);

        gbc.gridx = 1;
        txtCpf = new JTextField(20);
        add(txtCpf, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        btnSalvar = new JButton("Salvar");
        add(btnSalvar, gbc);

        gbc.gridx = 1;
        btnLimpar = new JButton("Limpar");
        add(btnLimpar, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        btnListar = new JButton("Listar Clientes");
        add(btnListar, gbc);

        btnSalvar.addActionListener(e -> salvarCliente());
        btnLimpar.addActionListener(e -> limparCampos());
        btnListar.addActionListener(e -> escolherListagem());
    }

    // 🔥 CONEXÃO (MySQL ou PostgreSQL)
    private Connection conectar() throws Exception {
        // MYSQL
        String url = "jdbc:mysql://localhost:3306/clientes_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "Senai@118";

        // POSTGRES (use se quiser)
        // String url = "jdbc:postgresql://localhost:5432/clientes_db";
        // String user = "postgres";
        // String password = "123456";

        return DriverManager.getConnection(url, user, password);
    }

    // 🔥 SALVAR NO BANCO + TXT
    private void salvarCliente() {
        try (Connection conn = conectar()) {

            String sql = "INSERT INTO clientes (nome, email, telefone, cpf) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, txtNome.getText());
            stmt.setString(2, txtEmail.getText());
            stmt.setString(3, txtTelefone.getText());
            stmt.setString(4, txtCpf.getText());

            stmt.executeUpdate();

            salvarEmTxt();

            JOptionPane.showMessageDialog(null, "Cliente salvo!");
            limparCampos();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
    }

    // 🔥 SALVAR TXT
    private void salvarEmTxt() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("clientes.txt", true))) {

            writer.write(txtNome.getText() + ";" +
                    txtEmail.getText() + ";" +
                    txtTelefone.getText() + ";" +
                    txtCpf.getText());
            writer.newLine();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar TXT!");
        }
    }

    // 🔥 ESCOLHER DE ONDE LISTAR
    private void escolherListagem() {
        String[] opcoes = {"Banco de Dados", "Arquivo TXT"};

        int escolha = JOptionPane.showOptionDialog(null,
                "De onde deseja listar?",
                "Listagem",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);

        if (escolha == 0) {
            listarClientesBanco();
        } else {
            listarClientesTxt();
        }
    }

    // 🔥 LISTAR DO BANCO
    private void listarClientesBanco() {
        try (Connection conn = conectar()) {

            String sql = "SELECT * FROM clientes";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            mostrarResultado(rs);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar BD!");
        }
    }

    // 🔥 LISTAR TXT
    private void listarClientesTxt() {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader("clientes.txt"))) {

            String linha;
            while ((linha = reader.readLine()) != null) {

                String[] d = linha.split(";");

                sb.append("Nome: ").append(d[0]).append("\n");
                sb.append("Email: ").append(d[1]).append("\n");
                sb.append("Telefone: ").append(d[2]).append("\n");
                sb.append("----------------------\n");
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao ler TXT!");
        }

        mostrarTexto(sb.toString());
    }

    // 🔥 REAPROVEITAMENTO DE EXIBIÇÃO
    private void mostrarResultado(ResultSet rs) throws Exception {
        StringBuilder sb = new StringBuilder();

        while (rs.next()) {
            sb.append("Nome: ").append(rs.getString("nome")).append("\n");
            sb.append("Email: ").append(rs.getString("email")).append("\n");
            sb.append("Telefone: ").append(rs.getString("telefone")).append("\n");
            sb.append("----------------------\n");
        }

        mostrarTexto(sb.toString());
    }

    private void mostrarTexto(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setEditable(false);
        JOptionPane.showMessageDialog(null, new JScrollPane(area));
    }

    private JFormattedTextField criarMascaraTelefone() {
        try {
            MaskFormatter mascara = new MaskFormatter("(##) #####-####");
            return new JFormattedTextField(mascara);
        } catch (ParseException e) {
            return new JFormattedTextField();
        }
    }

    private void limparCampos() {
        txtNome.setText("");
        txtEmail.setText("");
        txtTelefone.setValue(null);
        txtCpf.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CadastroClienteForm().setVisible(true));
    }
}