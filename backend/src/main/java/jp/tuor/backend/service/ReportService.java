package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.repository.ClienteRepository;
import jp.tuor.backend.service.exceptions.ClienteNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ClienteRepository clienteRepository;

    public byte[] gerarRelatorioGeralClientes() throws FileNotFoundException, JRException {
        List<Cliente> clientes = this.clienteRepository.findAll();
        return this.criarPdfDeJasper(clientes, "clientes.jasper");
    }

    public byte[] gerarRelatorioClienteIndividual(Long clienteId) throws FileNotFoundException, JRException {
        Optional<Cliente> cliente = clienteRepository
                .findById(clienteId);

        if(cliente.isPresent()) {
            Cliente cl = cliente.get();
            List<Cliente> clienteSingLs = Collections.singletonList(cl);

            return criarPdfDeJasper(clienteSingLs, "clientes.jasper");
        } else {
            throw new ClienteNaoEncontradoException("Cliente não encontrado: " + clienteId);
        }
    }

    private byte[] criarPdfDeJasper(List<Cliente> dataSourceList, String jasperFileName) throws FileNotFoundException, JRException {
        File reportDir = ResourceUtils.getFile("classpath:reports");
        String reportPath = reportDir.getAbsolutePath();

        File jasperFile = new File(reportPath + File.separator + jasperFileName);

        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperFile);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataSourceList);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("SUBREPORT_PATH", reportPath + File.separator);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
