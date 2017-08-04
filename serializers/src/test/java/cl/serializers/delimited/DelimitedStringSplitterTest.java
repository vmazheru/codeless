package cl.serializers.delimited;

import static cl.serializers.delimited.DelimitedStringSplitter.alwaysEnclosed;

import org.junit.Assert;
import org.junit.Test;

public class DelimitedStringSplitterTest {

	@Test
	public void testSplit() {
		
		assertCorrectSplit(
				"Split of string with comma-separated empty values, should result in an array of empty strings",
				",,",
				new String[]{"", "", ""},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Split of string with comma-separated spaced values, should result in an array of empty strings",
				"  , ,  ",
				new String[]{"", "", ""},
				DelimitedStringSplitter.csvTrimming()
		);
		assertCorrectSplit(
				"Split of string with comma-separated empty values, should return empty values",
				"abc,4,5,,,",
				new String[]{"abc", "4", "5", "", "", ""},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Values in the CSV file should be trimmed",
				"This , is , the, house , that , Jack, built",
				new String[] {"This", "is", "the", "house", "that", "Jack", "built"},
				DelimitedStringSplitter.csvTrimming()
		);
		
		assertCorrectSplit(
				"Leading/trailing spaces in double qoute enclosed CSV should be removed", 
				"      \"This\",\"is\",\"   the\",\"house   \",\"that\",\"Jack\",\"built\"      ",
				new String[] {"This", "is", "the", "house", "that", "Jack", "built"},
				DelimitedStringSplitter.csvTrimmingAll()
		);
		assertCorrectSplit(
				"Enclosing double qoutes should be removed in the result list values", 
				"\"This\",\"is\",\"the\",\"house\",\"that\",\"Jack\",\"built\"",
				new String[] {"This", "is", "the", "house", "that", "Jack", "built"},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Escaped double qoutes in the beginning and the end should reduce to single double qoutes",
				"\"He\",\"says\",\"\"\"wait!\"\"\"",
				new String[]{"He", "says", "\"wait!\""},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Escaped double qoutes in the middle should stay, and the enclosing double qoutes should be removed",
				"\"He\",\"says\",\" impatiantly \"\"wait!\"\".\"",
				new String[]{"He", "says", "impatiantly \"wait!\"."},
				DelimitedStringSplitter.csvTrimmingAll()
		);
		assertCorrectSplit(
				"Commas in escaped CSV should not be used for splitting",
				"\"He\",\"says\",\"\"wait, let me do it\"\"",
				new String[]{"He", "says", "\"wait, let me do it\""},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"A single double qouted value should preserve qoutes, since we don't know, whether it's a regular value or a qouted value",
				"\"Double qouted, with comma\"",
				new String[]{"Double qouted, with comma"},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Real-life case",
				"Keyword,Active,26399317594,1254625098,360043.Real_Estate_MB_Cannibalization,for rent in m_4677bcd0-c7ed-4167-a003-81ce6ba2e3eb,\"For Rent In Milford, pa\",Broad,0.22,http://www.ask.com/slp?&q=for+rent+in+milford%2Cpa&sid=4677bcd0-c7ed-4167-a003-81ce6ba2e3eb-0-us_msb&kwid={QueryString}&cid={AdID}",
				new String[]{"Keyword", "Active", "26399317594", "1254625098", "360043.Real_Estate_MB_Cannibalization", "for rent in m_4677bcd0-c7ed-4167-a003-81ce6ba2e3eb", "For Rent In Milford, pa", "Broad", "0.22", "http://www.ask.com/slp?&q=for+rent+in+milford%2Cpa&sid=4677bcd0-c7ed-4167-a003-81ce6ba2e3eb-0-us_msb&kwid={QueryString}&cid={AdID}"},
				DelimitedStringSplitter.csv()				
		);
		assertCorrectSplit(
				"Real-life case with two commas",
				"Keyword,Active,26471780444,1510840385,391516.Mod_Broad_Health_stale_KWs2,\" +ying +wu +md_8551f13a-c839-4f2d-99f8-cea7acc185c6\",,Active,\" +ying +wu +md, +bellingham, +wa\",Broad,0.06,http://www.ask.com/slp?&q=ying+wu+md%2C+bellingham%2C+wa&sid=8551f13a-c839-4f2d-99f8-cea7acc185c6-0-us_msb&kwid={QueryString}&cid={AdID},\" +ying +wu +md, +bellingham, +wa\",",
				new String[] {"Keyword", "Active", "26471780444", "1510840385", "391516.Mod_Broad_Health_stale_KWs2", "+ying +wu +md_8551f13a-c839-4f2d-99f8-cea7acc185c6","", "Active","+ying +wu +md, +bellingham, +wa", "Broad", "0.06", "http://www.ask.com/slp?&q=ying+wu+md%2C+bellingham%2C+wa&sid=8551f13a-c839-4f2d-99f8-cea7acc185c6-0-us_msb&kwid={QueryString}&cid={AdID}", "+ying +wu +md, +bellingham, +wa", ""},
				DelimitedStringSplitter.csvTrimmingAll()
		);
		assertCorrectSplit(
				"Real-life case with two double qoutes in the middle of the ad group name",
				"Ad Group,Active,541564237,71452891,215173.Recipes - Bing,\"venison \"\"saus_b36b5014-7a31-407c-8e0f-819898bfb964\"",
				new String[] {"Ad Group", "Active", "541564237", "71452891", "215173.Recipes - Bing", "venison \"saus_b36b5014-7a31-407c-8e0f-819898bfb964"},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Real-life case with three double qoutes in the beginning",
				"Ad Group,Active,542210386,71455054,215919.Adwise - Network,\"\"\"making cross_d7f4ead1-7a45-48b5-a26f-7859b3e2707d\"",
				new String[] {"Ad Group", "Active", "542210386", "71455054", "215919.Adwise - Network", "\"making cross_d7f4ead1-7a45-48b5-a26f-7859b3e2707d"},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Real-life case with 35\"",
				"Ad Group,Active,542286484,71455890,216049.QS8 - Network,\"35\"\" flat scre_b27805c1-3e09-4400-b7c1-1787e2dd4b99\"",
				new String[] {"Ad Group", "Active", "542286484", "71455890", "216049.QS8 - Network", "35\" flat scre_b27805c1-3e09-4400-b7c1-1787e2dd4b99"},
				DelimitedStringSplitter.csv()
		);
		assertCorrectSplit(
				"Real-life case with space in the beginning",
				"Ad Group,Active,1791805612,220012610,532706.Home&Garden_10-25_Bing,\" +12 +000 +btu_e792b527-1948-44b3-a7f0-f2e16de83aba\"",
				new String[] {"Ad Group", "Active", "1791805612", "220012610", "532706.Home&Garden_10-25_Bing", "+12 +000 +btu_e792b527-1948-44b3-a7f0-f2e16de83aba"},
				DelimitedStringSplitter.csvTrimmingAll()
		);
		assertCorrectSplit(
				"Real-life case with comma as first character",
				"Ad Group,Active,544568448,71611669,243525.Phase2_Set3_General_Bing,\", back pain ,_51106b60-c863-4325-a1f7-df7e361ffeaf\"",
				new String[] {"Ad Group", "Active", "544568448", "71611669", "243525.Phase2_Set3_General_Bing", ", back pain ,_51106b60-c863-4325-a1f7-df7e361ffeaf"},
				DelimitedStringSplitter.csvTrimmingAll()
		);
        assertCorrectSplit(
                "Split of string with |-separated empty values, should return empty values",
                "abc|4|5|||",
                new String[]{"abc", "4", "5", "", "", ""},
                DelimitedStringSplitter.pipe()
        );
        assertCorrectSplit(
                "Real-life case with pipe as first character",
                "Ad Group|Active|544568448|71611669|243525.Phase2_Set3_General_Bing|\"| back pain |_51106b60-c863-4325-a1f7-df7e361ffeaf\"",
                new String[] {"Ad Group", "Active", "544568448", "71611669", "243525.Phase2_Set3_General_Bing", "| back pain |_51106b60-c863-4325-a1f7-df7e361ffeaf"},
                DelimitedStringSplitter.pipe()
        );	    
        assertCorrectSplit(
                "Tab separated header",
                "\"Action\"\t\"Object Type\"\t\"Campaign ID\"\t\"Campaign\"\t\"Campaign Objective\"\t\"Campaign Budget\"\t\"Campaign Budget Type\"\t\"Channel\"\t\"Language\"\t\"Conversion Pixel\"\t\"Tracking Partner\"\t\"Ad Group ID\"\t\"Ad Group\"\t\"Object ID\"\t\"Location\"\t\"Device\"\t\"Interest\"\t\"Gender\"\t\"Ad Schedule\"\t\"Radius\"\t\"Bid Modifier\"\t\"Search CPC\"\t\"Stream CPC\"\t\"CPM\"\t\"Keyword\"\t\"Match Type\"\t\"Title\"\t\"Description\"\t\"Display URL\"\t\"Landing URL\"\t\"Impression Tracking URL\"\t\"Sponsored By\"\t\"Ad Image URL\"\t\"Ad HQ Image URL\"\t\"Sitelink Position\"\t\"param1\"\t\"param2\"\t\"param3\"\t\"Phone Number\"\t\"Country\"\t\"Call Only\"\t\"Latitude\"\t\"Longitude\"\t\"Start Date\"\t\"End Date\"\t\"Network\"\t\"Campaign Status\"\t\"Ad Group Status\"\t\"Status\"\t\"Error\"",
                new String[] {"Action", "Object Type", "Campaign ID", "Campaign", "Campaign Objective", "Campaign Budget", "Campaign Budget Type", "Channel", "Language", "Conversion Pixel", "Tracking Partner", "Ad Group ID", "Ad Group", "Object ID", "Location", "Device", "Interest", "Gender", "Ad Schedule", "Radius", "Bid Modifier", "Search CPC", "Stream CPC", "CPM", "Keyword", "Match Type", "Title", "Description", "Display URL", "Landing URL", "Impression Tracking URL", "Sponsored By", "Ad Image URL", "Ad HQ Image URL", "Sitelink Position", "param1", "param2", "param3", "Phone Number", "Country", "Call Only", "Latitude", "Longitude", "Start Date", "End Date", "Network", "Campaign Status", "Ad Group Status", "Status", "Error"},
                DelimitedStringSplitter.tab()
        );
        assertCorrectSplit(
                "Tab separated row",
                "\"ADD\"\t\"Ad Group\"\t\"255006\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"138012\"\t\"cash money ca_29e8c83a-43b2-4f13-8471-e0cabce087d8\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"0.05\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"2015-04-14\"\t\"2025-04-11\"\t\"\"\t\"\"\t\"Active\"\t\"\"\t\"\"",
                new String[] {"ADD", "Ad Group", "255006", "", "", "", "", "", "", "", "", "138012", "cash money ca_29e8c83a-43b2-4f13-8471-e0cabce087d8", "", "", "", "", "", "", "", "", "0.05", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "2015-04-14", "2025-04-11", "", "", "Active", "", ""},
                DelimitedStringSplitter.tab(false).with(alwaysEnclosed, true).locked()
        );
        assertCorrectSplit(
                "Another tab separated row",
                "\t\"Ad Group\"\t\"255006\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"138012\"\t\"cash money ca_29e8c83a-43b2-4f13-8471-e0cabce087d8\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"0.05\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"\"\t\"2015-04-14\"\t\"2025-04-11\"\t\"\"\t\"\"\t\"Active\"\t\"\"\t\"\"",
                new String[] {"", "Ad Group", "255006", "", "", "", "", "", "", "", "", "138012", "cash money ca_29e8c83a-43b2-4f13-8471-e0cabce087d8", "", "", "", "", "", "", "", "", "0.05", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "2015-04-14", "2025-04-11", "", "", "Active", "", ""},
                DelimitedStringSplitter.tab(false).with(alwaysEnclosed, true).locked()
        );
        assertCorrectSplit(
                "Space separated row",
                "1 2 3 4 5",
                new String[] {"1","2","3","4","5"},
                DelimitedStringSplitter.space()
        );
	}
	
	private static void assertCorrectSplit(String message, String s, String[] correct, DelimitedStringSplitter splitter) {
		String[] result = splitter.split(s);
		//printArray(result);
		Assert.assertArrayEquals(message, correct, result);
	}
	/*
	private static void printArray(String[] arr) {
		System.out.println(String.join("|", Arrays.asList(arr)));
	}
	*/
}
