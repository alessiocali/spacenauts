package com.gff.spacenauts.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class Packer {

	public static void main(String[] args){
		TexturePacker.Settings settings = new TexturePacker.Settings();
		settings.maxHeight = 2048;
		settings.maxWidth = 2048;
		TexturePacker.process(settings, "D:\\Documents\\GDX_Workspace\\assets\\Spacenauts\\Spacenauts-textures", "D:\\Documents\\GDX_Workspace\\Spacenauts\\src-Spacenauts\\android\\assets\\textures", "textures");
		TexturePacker.process(settings, "D:\\Documents\\GDX_Workspace\\assets\\Spacenauts\\Spacenauts-ui", "D:\\Documents\\GDX_Workspace\\Spacenauts\\src-Spacenauts\\android\\assets\\textures", "ui");
		TexturePacker.process(settings, "D:\\Documents\\GDX_Workspace\\assets\\Spacenauts\\Spacenauts-previews", "D:\\Documents\\GDX_Workspace\\Spacenauts\\src-Spacenauts\\android\\assets\\textures", "previews");
	}
	
}
