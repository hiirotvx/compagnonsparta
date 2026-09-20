package me.astero.companions.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;



public class ItemBuilderUtil {

	private ItemStack itemStack;

	public ItemBuilderUtil(Material material, String displayName, int stackAmount)
	{

		this.itemStack = new ItemStack(material, stackAmount);


		ItemMeta itemMeta = this.itemStack.getItemMeta();
		itemMeta.displayName(MessageUtil.parse(displayName));

		this.itemStack.setItemMeta(itemMeta);


	}

	public ItemBuilderUtil(ItemStack itemStack, String displayName)
	{

		this.itemStack = itemStack;


		ItemMeta itemMeta = this.itemStack.getItemMeta();
		itemMeta.displayName(MessageUtil.parse(displayName));

		this.itemStack.setItemMeta(itemMeta);


	}

	public ItemBuilderUtil setLore(List<String> list)
	{
		ItemMeta itemMeta = this.itemStack.getItemMeta();

		List<Component> componentLore = list.stream()
				.map(MessageUtil::parse)
				.collect(Collectors.toList());
		itemMeta.lore(componentLore);


		this.itemStack.setItemMeta(itemMeta);

		return this;
	}
	
	public ItemBuilderUtil setLore(String... lore)
	{
		ItemMeta itemMeta = this.itemStack.getItemMeta();

		List<Component> componentLore = Arrays.stream(lore)
				.map(MessageUtil::parse)
				.collect(Collectors.toList());
		itemMeta.lore(componentLore);


		this.itemStack.setItemMeta(itemMeta);

		return this;
	}
	
	public ItemBuilderUtil setGlow()
	{
		ItemMeta itemMeta = this.itemStack.getItemMeta();
		
		itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		
		return this;
	}
	
	
	public ItemStack build()
	{
		return this.itemStack;
	}

}
