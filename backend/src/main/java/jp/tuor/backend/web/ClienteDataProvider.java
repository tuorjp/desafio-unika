package jp.tuor.backend.web;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Iterator;

@RequiredArgsConstructor
public class ClienteDataProvider extends SortableDataProvider<Cliente, String> {
    private final ClienteService clienteService;
    private Page<Cliente> lastPageCache;

    @Override
    public Iterator<? extends Cliente> iterator(long first, long count) {
        int page = (int) (first / count);
        int size = (int) count;

        Pageable pageable = PageRequest.of(page, size);

        this.lastPageCache = clienteService.buscarClientesFiltrados(null, null, null, pageable);

        if(this.lastPageCache != null) {
            return this.lastPageCache.iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    @Override
    public long size() {
        if(this.lastPageCache == null) {
            iterator(0, 10); //valor padrão
        }

        return this.lastPageCache != null ? this.lastPageCache.getTotalElements() : 0;
    }

    @Override
    public IModel<Cliente> model(Cliente cliente) {
        final Long clienteId = cliente.getId();

        return new LoadableDetachableModel<Cliente>() {
            @Override
            protected Cliente load() {
                return clienteService.buscaClientePorId(clienteId);
            }
        };
    }
}
