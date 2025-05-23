package com.github.rfsmassacre.heavenrpg.data;

import com.github.rfsmassacre.heavenlibrary.paper.managers.PaperYamlStorage;
import com.github.rfsmassacre.heavenrpg.HeavenRPG;
import com.github.rfsmassacre.heavenrpg.classes.OriginClass;

public class ClassYaml extends PaperYamlStorage<OriginClass>
{
    public ClassYaml()
    {
        super(HeavenRPG.getInstance(), "classes", OriginClass.class);
    }
}
