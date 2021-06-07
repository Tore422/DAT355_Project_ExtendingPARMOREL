# Project PARMOREL

Personalized and Automatic Repair MOdels using REinforcement Learning (PARMOREL).

PARMOREL is a WIP tool for automatic software models repairing using reinforcement learning (Q-Learning at the moment).

Be aware of the random nature of Reinforcement Learning algorithm, this can lead to different results in different executions.

If you want to reset the algorithm's knowledge, just delete all content inside knowledge.xml.

Contact: abar@hvl.no

## How to download the project
1. Make sure you are running an Eclipse development environment version 2018-12 (4.10.0) or a compatible version.<br>
   Eclipse-packages can be found [here](https://www.eclipse.org/downloads/packages/).
3. Make sure it has Eclipse Modeling Tools and "Plug-in Development Environment" installed.
4. Make sure you are running Java version 13 or higher.
5. Clone the project and its submodules recursively: </br>
  `git clone --recursive <insert project URL here>` </br>
  If you are not familiar with submodules, you can have a look at [Vogella](https://www.vogella.com/tutorials/GitSubmodules/article.html) or at the [Git-documentation](https://git-scm.com/book/en/v2/Git-Tools-Submodules).
  
  
## How to use contents of this repository
This project is an attempt at extending PARMOREL to support repair of Ecore instance models.<br>

The code presented here needs to be combined with the rest of the plugin, found in repository: https://github.com/MagMar94/ParmorelEclipsePlugin

1. Clone the contents of the plugin repository and open it as an eclipse project.
2. Clone the contents of this repository, and replace the folder named projectparmorel in the plugin project<br>
   with the same folder from this project.
3. Open the file plugin.xml, and change the value on line 45 from 'value= "ecore"', to 'value= "*"'.
4. You can now run "ParmorelEclipsePlugin" as an eclipse application,<br> 
   where right clicking on an instance model offers the option Parmorel -> Repair.<br>
   * Note that attempting to repair a file that is not a model will cause PARMOREL to become unresponsive to further commands.<br>
   * Also note that this extension is not finished, and repairing instance models does not work as of yet.
   * Repairing Ecore meta-models should work as normal, but some errors might need to be resolved before the project can be compiled.

A broken instance model with some OCL constraint violations, can be found in the following repository<br>
along with a pdf file detailing more information related to this project: https://github.com/Tore422/DAT355_Project_ExtendingPARMOREL_TestingModelAndAdditionalResources






