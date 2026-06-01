USE zhexuan_divination;
SET NAMES utf8mb4;

DELETE FROM classic_chapter;
DELETE FROM classic_book;

INSERT INTO classic_book
(id, name, slug, author, dynasty, focus, description, source_url, source_note, copyright_status, sort_order, enabled, create_time, update_time)
VALUES
(1,'穷通宝鉴','qiong-tong-bao-jian','余春台辑','清','调候用神','以月令气候、寒暖燥湿为核心，适合判断命局急病和调候层次。','https://m.gushiwen.cn/guwen/book_112f16f9deeb.aspx','在线古籍整理页；后续可按章节校对导入。','古籍原文可整理，现代注释需确认授权',10,1,NOW(),NOW()),
(2,'滴天髓','di-tian-sui','京图撰，刘基注传','明清','体用清浊','重气势、清浊、源流、病药，适合提升整体结构判断。','https://guoxue3000.com/tw/books/378','常见版本含阐微、征义等评注，导入时需区分原文与注本。','古籍原文可整理，评注版本需确认授权',20,1,NOW(),NOW()),
(3,'三命通会','san-ming-tong-hui','万民英','明','格局神煞','兼收格局、神煞、纳音、岁运，可作综合资料索引。','https://www.huaxiashici.cn/guwen/sanmingtonghui/index.html','公开章节目录较多，适合逐卷校对。','古籍原文可整理',30,1,NOW(),NOW()),
(4,'八字提要','ba-zi-ti-yao','','近现代','分析清单','适合做四柱分析流程，先提纲再落宫位与岁运。','https://commons.wikimedia.org/wiki/File:NLC511-51003343-75959_%E5%85%AB%E5%AD%97%E6%8F%90%E8%A6%81.pdf','扫描 PDF 来源可查，但全文入库需注意近现代版权。','版权需确认，暂存提要与索引',40,1,NOW(),NOW()),
(5,'子平真诠','zi-ping-zhen-quan','沈孝瞻','清','月令格局','以月令格局、成格败格、顺逆用神为纲。','https://zh.wikipedia.org/wiki/%E5%AD%90%E5%B9%B3%E7%9C%9F%E8%A9%AE','需继续核对可靠原文底本。','古籍原文可整理，现代评注需确认授权',50,1,NOW(),NOW()),
(6,'渊海子平','yuan-hai-zi-ping','徐大升','宋','子平基础','以日主、十神、月令、岁运为基础的子平法框架。','https://www.gushicimingju.com/dianji/yuanhaiziping/','在线章节可检索；后续需按卷校对。','古籍原文可整理',60,1,NOW(),NOW()),
(7,'天元巫咸','tian-yuan-wu-xian','','古籍佚文/合编','干支象法','偏重天元气象、干支透藏与象法取类，需考证版本来源。','','独立稳定全文来源暂未确认，先建立目录位。','版本待考，暂存提要与索引',70,1,NOW(),NOW()),
(8,'神峰通考','shen-feng-tong-kao','张楠','明','病药中和','重病药、中和、从格辨析和格局活法。','https://commons.wikimedia.org/wiki/File:NLC416-13jh001619-43305_%E7%A5%9E%E5%B3%B0%E9%80%9A%E8%80%83.pdf','扫描 PDF 来源可用于人工校对。','古籍原文可整理',80,1,NOW(),NOW()),
(9,'千里命稿','qian-li-ming-gao','韦千里','民国','现代应用','便于把十神旺衰转成职业、婚恋、财务等现代断语。','https://ctext.org/wiki.pl?chapter=933376&if=gb','近现代作品，全文入库需谨慎。','版权需确认，暂存提要与索引',90,1,NOW(),NOW()),
(10,'五行精纪','wu-xing-jing-ji','廖中','宋','五行纳音','重五行流通、纳音象法，可作辅助应象。','https://commons.wikimedia.org/wiki/File:NLC892-2621-209348_%E4%BA%94%E8%A1%8C%E7%B2%BE%E7%B4%80_%E7%AC%AC1%E5%86%8A.pdf','Wikimedia 有多册扫描，可逐册 OCR 校对。','古籍原文可整理',100,1,NOW(),NOW()),
(11,'李虚中命书','li-xu-zhong-ming-shu','李虚中','唐/后世辑本','古法合参','早期年命、纳音、神煞体系，适合与子平法合参。','https://zh.wikisource.org/zh-hans/%E6%9D%8E%E8%99%9B%E4%B8%AD%E5%91%BD%E6%9B%B8_%28%E5%9B%9B%E5%BA%AB%E5%85%A8%E6%9B%B8%E6%9C%AC%29/%E5%8D%B7%E4%B8%8B','维基文库有四库全书本页面，可逐卷校对。','古籍原文可整理',110,1,NOW(),NOW());

INSERT INTO classic_chapter
(book_id, title, volume, chapter_order, original_text, plain_text, notes, source_url, content_status, create_time, update_time)
VALUES
(1,'导读：调候总纲','提要',10,'本书以四时月令为纲，重寒暖燥湿与调候取用。全文原文待按可靠底本分章校对导入。','先判断月令气候，再判断日主旺衰、格局成败。调候解决气候病，扶抑解决强弱病。','当前为阅读索引，不冒充完整原文。','https://m.gushiwen.cn/guwen/book_112f16f9deeb.aspx','OUTLINE',NOW(),NOW()),
(2,'导读：体用清浊','提要',20,'本书重体用、清浊、源流、病药。全文原文待区分原文、注、阐微后导入。','先看命局主气与结构，再看使命局流通成事的用处。','避免把后世评注与原文混为一谈。','https://guoxue3000.com/tw/books/378','OUTLINE',NOW(),NOW()),
(3,'导读：格局与神煞总库','提要',30,'《三命通会》卷帙较多，涉及纳音、神煞、格局、岁运等门类。全文待逐卷校对导入。','适合作为规则索引库，断事时仍以月令格局和喜忌为主。','后续可按卷一、卷二等建立目录。','https://www.huaxiashici.cn/guwen/sanmingtonghui/index.html','OUTLINE',NOW(),NOW()),
(4,'导读：四柱提纲','提要',40,'《八字提要》来源需确认授权，暂不导入全文。','可作为分析流程清单：提纲、日主、格局、用忌、六亲、岁运。','近现代书籍，全文入库前应确认版权。','https://commons.wikimedia.org/wiki/File:NLC511-51003343-75959_%E5%85%AB%E5%AD%97%E6%8F%90%E8%A6%81.pdf','OUTLINE',NOW(),NOW()),
(5,'导读：月令格局','提要',50,'《子平真诠》以月令取格、成败救应为纲。全文待核对底本后导入。','取用不能只看缺什么补什么，应围绕格局成败确定用神、相神、忌神。','现代评注版需单独标注。','https://zh.wikipedia.org/wiki/%E5%AD%90%E5%B9%B3%E7%9C%9F%E8%A9%AE','OUTLINE',NOW(),NOW()),
(6,'导读：子平基础','提要',60,'《渊海子平》为子平法重要基础。全文待按卷章校对导入。','以十神、月令、宫位、岁运把五行生克翻译成人事。','后续可拆成论天干、论地支、论十神等章节。','https://www.gushicimingju.com/dianji/yuanhaiziping/','OUTLINE',NOW(),NOW()),
(7,'导读：天元象法','提要',70,'《天元巫咸》独立稳定全文来源暂未确认，先保留书目与考证位。','可作为干支象法补充，但需服从四柱主线。','待确认版本后再导入正文。','','OUTLINE',NOW(),NOW()),
(8,'导读：病药中和','提要',80,'《神峰通考》重病药、中和、从格、化格等辨析。扫描本待 OCR 校对。','先寻命局病处，再看药是否得力，岁运是否助药或助病。','PDF 扫描适合做人工校对导入。','https://commons.wikimedia.org/wiki/File:NLC416-13jh001619-43305_%E7%A5%9E%E5%B3%B0%E9%80%9A%E8%80%83.pdf','OUTLINE',NOW(),NOW()),
(9,'导读：现代断事','提要',90,'《千里命稿》为近现代作品，暂不导入全文。','适合把传统十神转译为现代职业、婚恋、财务、学业等问题。','确认授权前只做索引和规则摘要。','https://ctext.org/wiki.pl?chapter=933376&if=gb','OUTLINE',NOW(),NOW()),
(10,'导读：五行纳音','提要',100,'《五行精纪》保存大量五行、纳音、神煞古法。扫描本待逐册校对。','纳音与神煞宜作旁证，不宜压过子平四柱主线。','Wikimedia 多册 PDF 可作为底本线索。','https://commons.wikimedia.org/wiki/File:NLC892-2621-209348_%E4%BA%94%E8%A1%8C%E7%B2%BE%E7%B4%80_%E7%AC%AC1%E5%86%8A.pdf','OUTLINE',NOW(),NOW()),
(11,'导读：年命古法','提要',110,'《李虚中命书》可作早期年命、纳音体系参考。全文待按维基文库卷次校对。','现代应用宜与子平法合参：子平主断，年命纳音旁证。','可优先从维基文库四库本导入。','https://zh.wikisource.org/zh-hans/%E6%9D%8E%E8%99%9B%E4%B8%AD%E5%91%BD%E6%9B%B8_%28%E5%9B%9B%E5%BA%AB%E5%85%A8%E6%9B%B8%E6%9C%AC%29/%E5%8D%B7%E4%B8%8B','OUTLINE',NOW(),NOW());
