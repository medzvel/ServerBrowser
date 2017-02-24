package gui.controllers.implementations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import data.SampServer;
import entities.SampServerSerializeable;
import interfaces.DataServiceInterface;
import logging.Logging;

public class ServerListAllController extends ServerListControllerMain
{
	private static Registry				registry;

	private static DataServiceInterface	remoteDataService;

	static
	{
		try
		{
			registry = LocateRegistry.getRegistry("164.132.193.101");
			remoteDataService = (DataServiceInterface) registry.lookup(DataServiceInterface.INTERFACE_NAME);
		}
		catch (RemoteException | NotBoundException e)
		{
			Logging.logger.log(Level.SEVERE, "Couldn't connect to RMI Server.", e);
		}
	}

	private static Object deserialzieAndDecompress(final byte[] data)
	{
		try
		{
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			final GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
			final ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);
			final Object object = objectInputStream.readObject();
			objectInputStream.close();
			return object;
		}
		catch (final IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void init()
	{
		super.init();

		try
		{
			servers.clear();

			final List<SampServerSerializeable> serializedServers = (List<SampServerSerializeable>) deserialzieAndDecompress(remoteDataService.getAllServers());
			servers.addAll(serializedServers.stream().map(server ->
			{
				final SampServer newServer = new SampServer(server);
				playersPlaying += newServer.getPlayers();
				maxSlots += newServer.getMaxPlayers();
				return newServer;
			}).collect(Collectors.toSet()));

			sortedServers.clear();
			sortedServers.addAll(filteredServers);
		}
		catch (final RemoteException e)
		{
			e.printStackTrace();
		}

		serverCount.setText(sortedServers.size() + "");
		playerCount.setText(playersPlaying + "");
		slotCount.setText((maxSlots - playersPlaying) + "");
	}

	@Override
	protected void displayMenu(final List<SampServer> selectedServers, final double posX, final double posY)
	{
		super.displayMenu(selectedServers, posX, posY);

		addToFavourites.setVisible(true);
		removeFromFavourites.setVisible(false);
	}
}