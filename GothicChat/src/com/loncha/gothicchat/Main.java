package com.loncha.gothicchat;

import org.bukkit.plugin.java.JavaPlugin;

import com.bringholm.nametagchanger.NameTagChanger;

import net.md_5.bungee.api.ChatColor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;
//import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
//import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
//import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Main extends JavaPlugin implements Listener {
	//Variables (listas, hashmap, etc...)
	public static boolean estadoOOC = true; 
	
	//Lista de comandos
	String[] chatTypes = {"hablar", "bajo", "susurrar", "gritar", "ooc", "gooc", "staff", "me", "do", "evento", "intentar", "dados", "apagarooc","ficha","duda","ayuda","sumarpuntos","restarpuntos"};
	
	//Arrays para el comando /ayuda
	String[] ayudaUser = {"/hablar, /h - Canal IC para hablar", "/bajo, /b - Canal IC para hablar bajo", "/susurrar, /s - Canal IC para susurrar", "/gritar, /g - Canal IC para gritar", "/me - Canal IC para acciones", "/do - Canal IC para rol de entorno", "/intentar - Comando de intentar acción", "/dados - Comando para tirar dados", "/duda - Comando para pedir ayuda al staff", "/ooc - Comando para hablar OOC", "/gooc - Comando para gritar OOC"};
	String[] ayudaAdmin = {"/hablar, /h - Canal IC para hablar", "/bajo, /b - Canal IC para hablar bajo", "/susurrar, /s - Canal IC para susurrar", "/gritar, /g - Canal IC para gritar", "/me - Canal IC para acciones", "/do - Canal IC para rol de entorno", "/intentar - Comando de intentar acción", "/dados - Comando para tirar dados", "/duda - Comando para pedir ayuda al staff", "/ooc - Comando para hablar OOC", "/gooc - Comando para gritar OOC", "/staff - Comando para hablar por el canal de staff", "/evento - Comando para hablar por eventos"};
	
	//Lista de distancias a las que se te escucha en cada canal (ordenado con respecto a chatTypes)
	int[] canalDistancias = {20, 8, 3, 45, 20, 45, 100000, 20, 20, 100000, 20, 20, 20};
	
	//Lista de colores para los comandos/canales
	ChatColor[] colores = {ChatColor.GOLD, ChatColor.DARK_PURPLE, ChatColor.YELLOW, ChatColor.BLUE, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.GREEN, ChatColor.RED, ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.GOLD};
	
	//Hashmap para saber en que canal está hablando el jugador
	HashMap<Player, Integer> actualChat = new HashMap<Player, Integer>();
	
	//Datos de la ficha de personaje
	HashMap<Player, String[]> datosFicha = new HashMap<Player, String[]>();
	HashMap<Player, Boolean> fichaCompletada = new HashMap<Player, Boolean>();
	String[] arrDatosFicha = new String[6];
	
	//Inventario (ficha de personaje)
	Inventory invFicha, invRegion, invGenero, invFichaCreada;
	
	//ArrayList de dudas y usuarios
	ArrayList<String> listaDudas = new ArrayList<String>();
	ArrayList<Player> playerDudas = new ArrayList<Player>();
	
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		File fichas = new File("plugins/Fichas");
		if (!fichas.exists()) {
			fichas.mkdir();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		actualChat.put(p, 0); //Pone a los players que se acaban de conectar en el canal de hablar
		
		fichaCompletada.put(p,false);
		datosFicha.put(p, new String[] {"","","","",""});
		
		try {
			File ficha = new File("plugins/Fichas/"+p.getName()+"ficha.txt");
			if (ficha.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(ficha));
				String readLine;
				int intContador = 0;
				String[] tempDatos = new String[6];
				while ((readLine = br.readLine()) != null) {
					tempDatos[intContador] = readLine;
					intContador++;
				}
				datosFicha.put(p, tempDatos);
				
				br.close();
			}
		}catch(Exception k) {
			k.printStackTrace();
		}
		
		checkFicha(p);
		
		if (datosFicha.get(p)[0] != "") {
			p.setDisplayName(datosFicha.get(p)[0]);
			
		}
		
		
		//INVENTARIO DE FICHA
		invFicha = Bukkit.createInventory(p, 9, "Ficha de personaje");
		
		//DISPLAY DE INVENTARIO DE LA FICHA DE PERSONAJE
		createDisplay(Material.PAPER, invFicha, 0, "Nombre", "Introduce el nombre de tu personaje");
		createDisplay(Material.REDSTONE_TORCH_ON, invFicha, 1, "Edad", "Introduce la edad de tu personaje");
		createDisplay(Material.APPLE, invFicha, 2, "Género", "Introduce el género de tu personaje");
		createDisplay(Material.WOOD_SWORD, invFicha, 3, "Región", "Elige la región de procedencia de tu personaje");
		createDisplay(Material.BOOK, invFicha, 4, "Descripción", "Introduce una descripción");
		createDisplay(Material.DIRT, invFicha, 8, "Guardar y salir", "Guarda tu ficha de personaje");
		
		//INVENTARIO DE RAZA
		invRegion = Bukkit.createInventory(p, 9, "Selección de región");
		
		//DISPLAY DE INVENTARIO PARA ELEGIR RAZA
		createDisplay(Material.PAPER, invRegion, 2, "Varant", "Humano de Varant");
		createDisplay(Material.PAPER, invRegion, 4, "Nordmar", "Humano de Nordmar");
		createDisplay(Material.PAPER, invRegion, 6, "Myrtana", "Humano de Myrtana");
		
		//INVENTARIO DE GÉNERO
		invGenero = Bukkit.createInventory(p, 9, "Selección de género");
		
		//DISPLAY DE INVENTARO PARA ELEGIR GÉNERO
		createDisplay(Material.APPLE, invGenero, 2, "Masculino", "Género masculino");
		createDisplay(Material.APPLE, invGenero, 4, "Femenino", "Género femenino");
		createDisplay(Material.APPLE, invGenero, 6, "NB", "Género no binario");
		
	}
	
	//Método para comprobar la ficha de personaje de un jugador
	public void checkFicha(Player p) {
		try {
			
			if (datosFicha.get(p)[0] != "" && datosFicha.get(p)[1] != "" && datosFicha.get(p)[2] != "" && datosFicha.get(p)[3] != "" && datosFicha.get(p)[4] != "") {
				fichaCompletada.put(p,true);
			} else {
				fichaCompletada.put(p, false);
			}
			
			if (!fichaCompletada.get(p) || fichaCompletada.get(p) == null) {
				fichaCompletada.put(p,false);
				datosFicha.put(p, new String[] {"","","","",""});
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//onCommand comprueba a que chat estás intentando cambiar y te pone en ese chat, también te manda un mensaje indicándote si has conseguido unirte al chat (tiene también el comando /dados, /intentar, /me y /do que funcionan algo diferente)
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Comprueba que el comando introducido está en la lista de canales de chat
		for (int i = 0; i < chatTypes.length; i++) {
			if (cmd.getName().equalsIgnoreCase(chatTypes[i])) {
				
				//CANAL DE STAFF
				if (cmd.getName().equalsIgnoreCase("staff") && sender.hasPermission("gchat.admin")) {
					if (sender instanceof Player) {
						Player p = (Player)sender;
						actualChat.put(p, i);
						sender.sendMessage("Ahora estás hablando por el canal " + colores[actualChat.get(p)+1] + chatTypes[actualChat.get(p)]);
					}
				}
				
				else if (cmd.getName().equalsIgnoreCase("staff") && !sender.hasPermission("gchat.admin")){ 
					sender.sendMessage(ChatColor.RED + "Este canal es solo para administradores");
				}
				
				
				//COMANDO /ME
				else if (cmd.getName().equalsIgnoreCase("me")) {
					/*	/me <mensaje>
					 *	Acción que hace el jugador, tiene el mismo rango que el canal de hablar
					 */
					
					//Si no has escrito ningún mensaje te indica como se utiliza el comando
					if (args.length == 0) {
						sender.sendMessage(ChatColor.LIGHT_PURPLE + "/me <mensaje>");
					}
					
					else {
						String rawAction = "";
						for (int j = 0; j < args.length; j++) {					
							rawAction += args[j] + " "; //Añade al string rawAction los argumentos del comando /me (el mensaje o acción)
						}
						rawAction = rawAction.trim();
						
						Player j = (Player) sender; //Castea el sender a player
						List<Entity> entities = j.getNearbyEntities(20, 20, 20); //Crea una lista de entidades con todas las entidades en un radio de 20 bloques a partir del jugador
						String message = message = colores[5] + j.getDisplayName() + " " + rawAction; //Formatea el mensaje
						
						//Itera por todas las entidades de la lista y comprueba si están en rango, si lo están les envía el mensaje
						for(Entity e: entities) {
							if (e.getWorld().equals(j.getWorld())) {
								if (e.getLocation().distance(j.getLocation()) <= canalDistancias[7]) {	
									e.sendMessage(message);			
								}
							}
						}
						
						j.sendMessage(message); //Le envía el mensaje al player para que también pueda leerlo
						rawAction = ""; //Resetea la variable rawAction
					}
						
				}
				
				//COMANDO /DO
				else if (cmd.getName().equalsIgnoreCase("do")) {
					/*	/me <mensaje>
					 *	Acción que hace el jugador, tiene el mismo rango que el canal de hablar
					 */
					
					//Si no has escrito ningún mensaje te indica como se utiliza el comando
					if (args.length == 0) {
						sender.sendMessage(ChatColor.GREEN + "/do <mensaje>");
					}
					
					else {
						String rawAction = "";
						for (int j = 0; j < args.length; j++) {					
							rawAction += args[j] + " "; //Añade al string rawAction los argumentos del comando /me (el mensaje o acción)
						}
						rawAction = rawAction.trim();
						
						Player j = (Player) sender; //Castea el sender a player
						List<Entity> entities = j.getNearbyEntities(20, 20, 20); //Crea una lista de entidades con todas las entidades en un radio de 20 bloques a partir del jugador
						String message = message = colores[6] + " " + rawAction; //Formatea el mensaje
						
						//Itera por todas las entidades de la lista y comprueba si están en rango, si lo están les envía el mensaje
						for(Entity e: entities) {
							if (e.getWorld().equals(j.getWorld())) {
								if (e.getLocation().distance(j.getLocation()) <= canalDistancias[8]) {	
									e.sendMessage(message);			
								}
							}
						}
						
						j.sendMessage(message); //Le envía el mensaje al player para que también pueda leerlo
						rawAction = ""; //Resetea la variable rawAction
					}
						
				}
				
				//COMANDO /DUDA
				else if (cmd.getName().equalsIgnoreCase("duda")) {
					/*	/duda <mensaje>
					 *	Comando para enviar dudas a la administración
					 */
					
					//Si no has escrito ningún mensaje te indica como se utiliza el comando
					Player j = (Player) sender;
					boolean bolSendeable = false;
					boolean unavez = true;
					if (args.length == 0) {
						j.sendMessage(ChatColor.GOLD + "/duda <mensaje>");
					}
					
					else {
						String rawAction = "";
						for (int k = 0; k < args.length; k++) {
							if (sender.hasPermission("gchat.admin")) {
								if (args[0].equalsIgnoreCase("ver")) {
									if (listaDudas.size() > 0) {
										for(int l = 0; l < listaDudas.size(); l++) {
											j.sendMessage(ChatColor.GOLD + "Duda "+ l + ChatColor.RED + " Usuario " + playerDudas.get(l) + ChatColor.WHITE  + ": "+listaDudas.get(l));
										}
									} else {
										j.sendMessage(ChatColor.DARK_RED + "No quedan dudas por responder");
									}
								}
								
								else if (args[0].equalsIgnoreCase("responder")) {
									if (isNumeric(args[1])){
										int temp = Integer.parseInt(args[1]);
										
										if (temp <= listaDudas.size()) {
											for(int p = 2; p < args.length; p++) {
												rawAction += args[p] + " ";
											}
											if (unavez) {
												unavez = false;
												playerDudas.get(temp).sendMessage(ChatColor.GOLD + "Respuesta de un administrador: " + ChatColor.WHITE + rawAction);
												playerDudas.remove(temp);
												listaDudas.remove(temp);
											}
										}
									}
								}
							} else {
								if (args.length >= 4) {
									rawAction += args[k] + " "; //Añade al string rawAction los argumentos del comando /duda (el mensaje o acción)
									bolSendeable = true;
								}
								else {
									sender.sendMessage("Tu duda tiene que superar las 3 palabras");
								}
							}
							
						}
						
						rawAction = rawAction.trim();
						
						
						if (bolSendeable) {
							j.sendMessage("Tu duda ha sido enviada con éxito"); //Le envía el mensaje al player para que también pueda leerlo
							
							listaDudas.add(rawAction);
							playerDudas.add(j);
							
							for(Player p : Bukkit.getServer().getOnlinePlayers()) {
								p.sendMessage(ChatColor.RED + "Hay una duda nueva");
							}
						}
						rawAction = ""; //Resetea la variable rawAction
					}
						
				}
									
				//COMANDO DE TIRAR DADOS
				else if (cmd.getName().equalsIgnoreCase("dados")){
					int resultadoTirada = (int) Math.floor(Math.random()*(20-1+1)+1);
					
					Player j = (Player) sender; //Castea el sender a player
					List<Entity> entities = j.getNearbyEntities(20, 20, 20); //Crea una lista de entidades con todas las entidades en un radio de 20 bloques a partir del jugador
					String message = ChatColor.GOLD + j.getDisplayName() + " tira los dados y saca: " + ChatColor.GREEN + resultadoTirada; 					
					//Itera por todas las entidades de la lista y comprueba si están en rango, si lo están les envía el mensaje
					
					for(Entity e: entities) {
						if (e.getWorld().equals(j.getWorld())) {
							if (e.getLocation().distance(j.getLocation()) <= canalDistancias[10]) {	
								e.sendMessage(message);			
							}
						}
					}
					
					j.sendMessage(message); //Le envía el mensaje al player para que también pueda leerlo
				}
				
				//COMANDO DE INTENTAR ACCIÓN
				else if (cmd.getName().equalsIgnoreCase("intentar")){
					double randomTry = Math.random();
					
					Player j = (Player) sender; //Castea el sender a player
					List<Entity> entities = j.getNearbyEntities(20, 20, 20); //Crea una lista de entidades con todas las entidades en un radio de 20 bloques a partir del jugador
					String message;
					
					if (randomTry < 0.5) {
						message = ChatColor.GREEN + j.getDisplayName() + " ha conseguido realizar la acción."; //Formatea el mensaje;
					}else {
						message = ChatColor.RED + j.getDisplayName() + " no ha conseguido realizar la acción."; //Formatea el mensaje;
					}
					
					//Itera por todas las entidades de la lista y comprueba si están en rango, si lo están les envía el mensaje
					for(Entity e: entities) {
						if (e.getWorld().equals(j.getWorld())) {
							if (e.getLocation().distance(j.getLocation()) <= canalDistancias[7]) {	
								e.sendMessage(message);			
							}
						}
					}
					
					j.sendMessage(message); //Le envía el mensaje al player para que también pueda leerlo		
					
				}
				
				else if (cmd.getName().equalsIgnoreCase("apagarooc")) {
					Player p = (Player) sender;
					
					if (p.hasPermission("gchat.admin")) {
						estadoOOC = !estadoOOC;
						p.sendMessage("El OOC está: " + estadoOOC);
					}
				}
				
				//COMANDO DE /AYUDA
				else if (cmd.getName().equalsIgnoreCase("ayuda")) {
					Player j = (Player) sender; //Castea el sender a player
					
					//Comprueba si el sender es admin o no (si es admin mostrará unos comandos, y si no lo está mostrará otros)
					if (j.hasPermission("gchat.admin")) {
						for (int k = 0; k < ayudaAdmin.length; k++) {
							j.sendMessage(ChatColor.AQUA + ayudaAdmin[k]);
						}
					} else {
						for (int k = 0; k < ayudaUser.length; k++) {
							j.sendMessage(ChatColor.AQUA + ayudaUser[k]);
						}
					}
				}
				
				//COMANDO PARA CREAR LA FICHA
				else if (cmd.getName().equalsIgnoreCase("ficha")) {
					Player p = (Player)sender;
					
					if(args.length < 1) {
						if (!fichaCompletada.get(p) || fichaCompletada.get(p) == null) {
							abrirInventario(p);
						} else {
							p.sendMessage("Ya has completado la ficha de personaje");
						}
					} else {
						if (args[0].equalsIgnoreCase("reset")) {
							if (p.hasPermission("gchat.admin")) {
								fichaCompletada.put(Bukkit.getServer().getPlayer(args[1]),false);
								datosFicha.put(Bukkit.getServer().getPlayer(args[1]), new String[] {"","","","",""});
								p.sendMessage("Ficha del jugador "+ args[1] + " reseteada");
							} else {
								p.sendMessage("No puedes usar ese comando");
							}
						} else if (args[0].equalsIgnoreCase("ver")) {
							
							if (args.length < 2) {
								if (fichaCompletada.get(p)) {
									invFichaCreada = Bukkit.createInventory(p, 9, "Ficha de personaje creado");
									
									createDisplay(Material.PAPER, invFichaCreada, 0, ChatColor.GOLD+"Nombre: ", datosFicha.get(p)[0]);
									createDisplay(Material.REDSTONE_TORCH_ON, invFichaCreada, 1, ChatColor.GOLD+"Edad: ", datosFicha.get(p)[1]);
									createDisplay(Material.APPLE, invFichaCreada, 2, ChatColor.GOLD+"Género: ", datosFicha.get(p)[2]);
									createDisplay(Material.WOOD_SWORD, invFichaCreada, 3, ChatColor.GOLD+"Región: ", datosFicha.get(p)[3]);
									createDisplay(Material.BOOK, invFichaCreada, 4, ChatColor.GOLD+"Descripción: ", datosFicha.get(p)[4]);
									createDisplay(Material.BOOK, invFichaCreada, 8, ChatColor.GOLD+"Puntos de rol: ", datosFicha.get(p)[5]);
									
									p.openInventory(invFichaCreada);
								} else {
									p.sendMessage("Todavía no has completado la ficha de personaje");
								}
							} else {
								if (fichaCompletada.get(Bukkit.getServer().getPlayer(args[1]))) {
									invFichaCreada = Bukkit.createInventory(p, 9, "Ficha de personaje creado");
									
									createDisplay(Material.PAPER, invFichaCreada, 0, ChatColor.GOLD+"Nombre: ", datosFicha.get(Bukkit.getServer().getPlayer(args[1]))[0]);
									createDisplay(Material.REDSTONE_TORCH_ON, invFichaCreada, 1, ChatColor.GOLD+"Edad: ", datosFicha.get(Bukkit.getServer().getPlayer(args[1]))[1]);
									createDisplay(Material.APPLE, invFichaCreada, 2, ChatColor.GOLD+"Género: ", datosFicha.get(Bukkit.getServer().getPlayer(args[1]))[2]);
									createDisplay(Material.WOOD_SWORD, invFichaCreada, 3, ChatColor.GOLD+"Región: ", datosFicha.get(Bukkit.getServer().getPlayer(args[1]))[3]);
									createDisplay(Material.BOOK, invFichaCreada, 4, ChatColor.GOLD+"Descripción: ", datosFicha.get(Bukkit.getServer().getPlayer(args[1]))[4]);
									createDisplay(Material.BOOK, invFichaCreada, 8, ChatColor.GOLD+"Puntos de rol: ", datosFicha.get(Bukkit.getServer().getPlayer(args[1]))[5]);
									
									p.openInventory(invFichaCreada);
								} else {
									p.sendMessage("El jugador "+args[1]+" todavía no ha completado su ficha de personaje");
								}
							}
						}
						
					}
				}
				
				else if (cmd.getName().equalsIgnoreCase("sumarpuntos")) {
					Player p = (Player) sender;
					
					if (p.hasPermission("gchat.admin")) {
						if (args.length < 2) {
							p.sendMessage("Uso: /sumarpuntos <usuario> <puntos>");
						} else {
							
							try {
								
								Player usuario = Bukkit.getServer().getPlayer(args[0]);
								
								int puntos = Integer.parseInt(args[1]);
								
								int puntosActuales = Integer.parseInt(datosFicha.get(usuario)[5]);
								
								puntosActuales+=puntos;
								
								datosFicha.put(usuario, new String[] {datosFicha.get(usuario)[0], datosFicha.get(usuario)[1], datosFicha.get(usuario)[2], datosFicha.get(usuario)[3], datosFicha.get(usuario)[4], Integer.toString(puntosActuales)}); 
								saveDatosFicha(usuario);
								p.sendMessage("Has "+ChatColor.GREEN+"sumado " + puntos + " puntos de rol "+ChatColor.WHITE+"a "+ ChatColor.GOLD+usuario.getName());
								usuario.sendMessage(ChatColor.GREEN+"Te han sumado " + puntos + " puntos de rol");
								
							} catch(Exception e) {
								p.sendMessage("Uso: /sumarpuntos <usuario> <puntos>");
							}
						}
					} else {
						p.sendMessage(ChatColor.RED+"No puedes usar ese comando");
					}
				}
				
				else if (cmd.getName().equalsIgnoreCase("restarpuntos")) {
					Player p = (Player) sender;
					
					if (p.hasPermission("gchat.admin")) {
						if (args.length < 2) {
							p.sendMessage("Uso: /restarpuntos <usuario> <puntos>");
						} else {
							
							try {
								
								Player usuario = Bukkit.getServer().getPlayer(args[0]);
								
								int puntos = Integer.parseInt(args[1]);
								
								int puntosActuales = Integer.parseInt(datosFicha.get(usuario)[5]);
								
								puntosActuales-=puntos;
								
								datosFicha.put(usuario, new String[] {datosFicha.get(usuario)[0], datosFicha.get(usuario)[1], datosFicha.get(usuario)[2], datosFicha.get(usuario)[3], datosFicha.get(usuario)[4], Integer.toString(puntosActuales)}); 
								saveDatosFicha(usuario);
								p.sendMessage("Has "+ChatColor.RED+"restado " + puntos + " puntos de rol"+ChatColor.WHITE+" a "+ ChatColor.GOLD+usuario.getName());
								usuario.sendMessage(ChatColor.RED+"Te han restado " + puntos + " puntos de rol");
							} catch(Exception e) {
								p.sendMessage("Uso: /restarpuntos <usuario> <puntos>");
							}
						}
					} else {
						p.sendMessage(ChatColor.RED+"No puedes usar ese comando");
					}
				}
								
				//RESTO DE CANALES DEL CHAT	
				else if (!cmd.getName().equalsIgnoreCase("staff") && !cmd.getName().equalsIgnoreCase("apagarooc")) {
					Player p = (Player) sender;
					if (sender instanceof Player) {
						actualChat.put(p, i);
					
						if (actualChat.get(p) != 7 && actualChat.get(p) != 8 && actualChat.get(p) != 13) {
							sender.sendMessage("Ahora estás hablando por el canal " + colores[actualChat.get(p)+1] + chatTypes[actualChat.get(p)]);
						}
					}
				}
				
				
				
				return true;
			}			
		}
		return false;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		
		Location playerLocation = event.getPlayer().getLocation();
		String rawMessage, message = "";
		
		
		//Crea y formatea el mensaje
		
		//Comprueba si estás escribiendo por el canal de acciones /me
		
		if (actualChat.get(player) != -1 && actualChat.get(player) != -2 && actualChat.get(player) != -3 && actualChat.get(player) != 7 && actualChat.get(player) != 8 && actualChat.get(player) != 10 && actualChat.get(player) != 11) { 
			rawMessage = event.getMessage();
			
			//Comprueba si estás hablando por el canal de evento
			if (actualChat.get(player) == 9) {			
				message = ChatColor.BOLD + "" + ChatColor.GOLD + "[EVENTO] " + "\\ " + rawMessage + " /";
			}
			
			//ACCIONES EN MITAD DE UNA FRASE
			else if (actualChat.get(player) != -1 && actualChat.get(player) != -2 && actualChat.get(player) != -3 && actualChat.get(player) != 9 && actualChat.get(player) != 8 && actualChat.get(player) != 7 && actualChat.get(player) != 6 && actualChat.get(player) != 5 && actualChat.get(player) != 4 && actualChat.get(player) != -1 && actualChat.get(player) != -2 && actualChat.get(player) != -3 && actualChat.get(player) != -4 && actualChat.get(player) != -5 && actualChat.get(player) != -6){
				
				ArrayList<String> arrMessage = new ArrayList<String>();
				StringBuilder str = new StringBuilder();
				int contador = 0;
				boolean checker = false;
				
				for (char c: rawMessage.toCharArray()) {
					if (c == '-' && str.toString().length() == 0) {
						checker = true;
						str.append(c);
					}
					
					else if (c == '-' && str.toString().length() > 1) {
						checker = true;
						if (str.charAt(0) == '-') {
							str.append(c);
							arrMessage.add(contador, str.toString());
							contador++;
							str.setLength(0);
						}else {
							arrMessage.add(contador, str.toString());
							contador++;
							str.setLength(0);
							str.append(c);
						}
						
					} else if (c != '-') {
						str.append(c);
						checker = false;
					}
	
				}
				
				if (!checker) arrMessage.add(contador, str.toString());
				
				message = colores[actualChat.get(player)+1] + "["+chatTypes[actualChat.get(player)].toUpperCase()+"] " + colores[0] + event.getPlayer().getDisplayName() + ChatColor.WHITE + ": ";
				for(String s: arrMessage) {
					if (s.charAt(0) == '-') {
						message += ChatColor.ITALIC + "" + ChatColor.LIGHT_PURPLE + s + " ";
					}else {
						message += ChatColor.WHITE + s + " ";
					}
				}

			}
			
			else {
				message = colores[actualChat.get(player)+1] + "["+chatTypes[actualChat.get(player)].toUpperCase()+"] " + colores[0] + event.getPlayer().getDisplayName() + ChatColor.WHITE + ": " + rawMessage;
			}
			
		}
		
		
		//Manda el mensaje a los players que estén dentro de la distancia permitida
		for (Player p : event.getRecipients()) {
			
			//Canal de administración (entre mundos, sin límite de distancia)
			if (actualChat.get(player) == 6) {
				if (p.hasPermission("gchat.admin")) {
					p.sendMessage(message);
				}
			}
			
			//Canal de eventos
			else if (actualChat.get(player) == 9) {
				p.sendMessage(message);
			}
			
			else if (actualChat.get(player) == -1) {
				String mensajeFicha = event.getMessage();
				player.sendMessage(mensajeFicha);
				actualChat.put(player, 0);
				datosFicha.put(player, new String[] {mensajeFicha, datosFicha.get(player)[1], datosFicha.get(player)[2], datosFicha.get(player)[3], datosFicha.get(player)[4],"0"});
				player.openInventory(invFicha);
			}
			
			else if (actualChat.get(player) == -2) {
				String mensajeFicha = event.getMessage();
				player.sendMessage(mensajeFicha);
				actualChat.put(player, 0);
				datosFicha.put(player, new String[] {datosFicha.get(player)[0], mensajeFicha, datosFicha.get(player)[2], datosFicha.get(player)[3], datosFicha.get(player)[4],"0"});
				player.openInventory(invFicha);
			}
			
			else if (actualChat.get(player) == -3) {
				String mensajeFicha = event.getMessage();
				player.sendMessage(mensajeFicha);
				actualChat.put(player, 0);
				datosFicha.put(player, new String[] {datosFicha.get(player)[0], datosFicha.get(player)[1], datosFicha.get(player)[2], datosFicha.get(player)[3], mensajeFicha,"0"});
				player.openInventory(invFicha);
			}
			
			else if (actualChat.get(player) == -4) {
				String mensajeFicha = event.getMessage();
				player.sendMessage(mensajeFicha);
				actualChat.put(player, 0);
				datosFicha.put(player, new String[] {datosFicha.get(player)[0], datosFicha.get(player)[1], datosFicha.get(player)[2], datosFicha.get(player)[3], mensajeFicha,datosFicha.get(player)[5]});
				saveDatosFicha(player);
				player.openInventory(invFicha);
			}
			
			//Otros canales
			else {
				if (p.getWorld().equals(player.getWorld())) {
					if (p.getLocation().distance(playerLocation) <= canalDistancias[actualChat.get(player)]) {
						if (actualChat.get(player) != 7 && actualChat.get(player) != 8 && actualChat.get(player) != 10 && actualChat.get(player) != 11) {
							if (actualChat.get(player) == 4) {
								if (estadoOOC) {
									p.sendMessage(message);
								} else {
									p.sendMessage("El OOC está desactivado");
								}
							} else {
								p.sendMessage(message);
							}
						}
					}
				}
			}
			
		}
		
		event.getRecipients().clear();
	}
	
	//FICHAS DE PERSONAJE
	public void abrirInventario(Player p) {
		p.openInventory(invFicha);
	}
	
	
	public static void createDisplay(Material material, Inventory inv, int Slot, String name, String lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		ArrayList<String> Lore = new ArrayList<String>();
		Lore.add(lore);
		meta.setLore(Lore);
		item.setItemMeta(meta);
		 
		inv.setItem(Slot, item); 
		 
	}
	
	//CONTROL DE CLICK EN LOS DIFERENTES INVENTARIOS GUI
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked(); // The player that clicked the item
		ItemStack clicked = event.getCurrentItem(); // The item that was clicked
		Inventory inventory = event.getInventory(); // The inventory that was clicked in
		
		if (inventory.getName().equals("Ficha de personaje")) {
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Nombre")) {
				//Cambia el chat a -1
				actualChat.put(player, -1);
				player.closeInventory();
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Edad")) {
				actualChat.put(player, -2);
				player.closeInventory();
				
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Género")) {
				player.closeInventory();
				player.openInventory(invGenero);
				
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Región")) {
				player.closeInventory();
				player.openInventory(invRegion);
				
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Descripción")) {
				actualChat.put(player, -3);
				player.closeInventory();
				
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Guardar y salir")) {
				if (datosFicha.get(player)[0] != "" && datosFicha.get(player)[1] != "" && datosFicha.get(player)[2] != "" && datosFicha.get(player)[3] != "" && datosFicha.get(player)[4] != "") {
					fichaCompletada.put(player,true);
					player.setDisplayName(datosFicha.get(player)[0]);
					player.sendMessage("Has completado la ficha de personaje");
					
					datosFicha.put(player, new String[] {datosFicha.get(player)[0],datosFicha.get(player)[1],datosFicha.get(player)[2],datosFicha.get(player)[3],datosFicha.get(player)[4],"0"});
					saveDatosFicha(player);
					
				} else {
					datosFicha.put(player, new String[] {"","","","","",""});
					fichaCompletada.put(player,false);
					player.sendMessage("No has completado la ficha de personaje");
				}
				
				player.closeInventory();
				
			}
		} else if (inventory.getName().equals("Selección de región")) {
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Varant")) {
				player.closeInventory();
				
				datosFicha.put(player, new String[] {datosFicha.get(player)[0],datosFicha.get(player)[1],datosFicha.get(player)[2],clicked.getItemMeta().getDisplayName(),datosFicha.get(player)[4], "0"});
				player.sendMessage(datosFicha.get(player)[3]);
				player.openInventory(invFicha);
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Nordmar")) {
				player.closeInventory();

				datosFicha.put(player, new String[] {datosFicha.get(player)[0],datosFicha.get(player)[1],datosFicha.get(player)[2],clicked.getItemMeta().getDisplayName(),datosFicha.get(player)[4], "0"});
				player.sendMessage(datosFicha.get(player)[3]);
				player.openInventory(invFicha);
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Myrtana")) {
				player.closeInventory();

				datosFicha.put(player, new String[] {datosFicha.get(player)[0],datosFicha.get(player)[1],datosFicha.get(player)[2],clicked.getItemMeta().getDisplayName(),datosFicha.get(player)[4], "0"});
				player.sendMessage(datosFicha.get(player)[3]);
				player.openInventory(invFicha);
			}
			
		} else if (inventory.getName().equals("Selección de género")) {
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Masculino")) {
				player.closeInventory();
				
				datosFicha.put(player, new String[] {datosFicha.get(player)[0],datosFicha.get(player)[1],clicked.getItemMeta().getDisplayName(),datosFicha.get(player)[3],datosFicha.get(player)[4], "0"});
				player.sendMessage(datosFicha.get(player)[2]);
				player.openInventory(invFicha);
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("Femenino")) {
				player.closeInventory();
				
				datosFicha.put(player, new String[] {datosFicha.get(player)[0],datosFicha.get(player)[1],clicked.getItemMeta().getDisplayName(),datosFicha.get(player)[3],datosFicha.get(player)[4], "0"});
				player.sendMessage(datosFicha.get(player)[2]);
				player.openInventory(invFicha);
			}
			
			if (clicked.getItemMeta().getDisplayName().equalsIgnoreCase("NB")) {
				player.closeInventory();
				
				datosFicha.put(player, new String[] {datosFicha.get(player)[0],datosFicha.get(player)[1],clicked.getItemMeta().getDisplayName(),datosFicha.get(player)[3],datosFicha.get(player)[4], "0"});
				player.sendMessage(datosFicha.get(player)[2]);
				player.openInventory(invFicha);
			}
		} else if (inventory.getName().equals("Ficha de personaje creado")) {
			event.setCancelled(true);			
		}
	}
	
	public static boolean isNumeric(String strNum) {
	    try {
	        double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException | NullPointerException nfe) {
	        return false;
	    }
	    return true;
	}
	
	public void saveDatosFicha(Player player) {
		//CÓDIGO PARA GUARDAR LA FICHA EN UN ARCHIVO
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("plugins/Fichas/"+player.getName()+"ficha.txt"));
			
			
			for(int i = 0; i < datosFicha.get(player).length; i++) {	
				bw.write(datosFicha.get(player)[i]+"\n");
			}

			bw.close();
		
		} catch(Exception e) {
			
		}
	}

}
