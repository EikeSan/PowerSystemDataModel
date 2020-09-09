/**
 * //ToDo: Class Description
 *
 * @version 0.1* @since 09.09.20
 */
class asd {

    static void main(String[] args){


        def lst = ["asd","asdf","peter"]

        println lst.contains("peter")

        String a = "commit 9cbd2d13f1abddeba0385d2fdcad834a5321426e (test-hotfix-copy)\n" +
                "Merge: f3e96145 151c0270\n" +
                "Author: johanneshiry <johannes.hiry@tu-dortmund.de>\n" +
                "Date:   Wed Sep 9 19:46:56 2020 +0200\n" +
                "\n" +
                "    Merge pull request #2 from johanneshiry/hotfix/jh/#000-test-hotfix-1\n" +
                "    \n" +
                "    Hotfix/jh/#000 test hotfix 1\n"

        a.split("\\s")



        println(a.split("\\s"))
        println(a.split("\\s").length)
        println(a.split("\\s")[5])
        println(a.split("\\s")[38])

        def hotfix_pattern = ".*(#\\d+).*"

        String test = "Hotfix/jh/#000"

        println ((test =~ ".*(#\\d+).*")[0][1])
//    def names= 'Bharath-Vinayak-Harish-Punith'
//    assert "Bharath" == (names =~ /^(.*?)\-/)[0][1]

    }

}
