package net.data.technology.jraft;

import java.nio.channels.CompletionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.extensions.Tuple2;

/**
 */
public final class AIOConnector
	implements CompletionHandler<Void, Tuple2<FrontendConnection, BackendConnection>> {
    private static final Logger logger = LoggerFactory.getLogger(AIOConnector.class);
    private static final IdGenerator ID_GENERATOR = new IdGenerator();

    public AIOConnector() {
        super();
    }

    @Override
    public void completed(Void result, Tuple2<FrontendConnection, BackendConnection> attachment) {
	attachment._1().setBackendConnection(attachment._2());
	attachment._2().setId(ID_GENERATOR.getId());
	attachment._2().setFrontendConnection(attachment._1());
	try {
	    attachment._2().asynRead();
	} catch (Exception e) {
	    failed(e, attachment);
	}
    }

    @Override
    public void failed(Throwable exc, Tuple2<FrontendConnection, BackendConnection> attachment) {
	attachment._1().close("connect is error:" + exc.getMessage());
	attachment._2().close("connect is error:" + exc.getMessage());
    }
}
